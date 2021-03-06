package api

import akka.http.scaladsl.server.Directives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.merit.modules.sales.SaleService
import com.merit.modules.products.ProductService
import com.merit.modules.brands.BrandService
import com.merit.modules.excel.ExcelService
import scala.concurrent.ExecutionContext
import api.sales.SaleRoutes
import akka.http.scaladsl.server.Route
import com.merit.api.AuthDirectives
import com.merit.api.users.LoginRoute
import com.merit.modules.users.UserService
import com.merit.api.users.UserRoutes
import com.merit.modules.categories.CategoryService
import com.merit.api.stockOrders.StockOrderRoutes
import com.merit.modules.stockOrders.StockOrderService
import com.merit.api.sales.SyncSaleRoute
import akka.http.scaladsl.server.directives.Credentials
import com.merit.AppConfig
import com.merit.api.stockOrders.SyncStockOrderRoute
import com.merit.api.brands.BrandRoutes
import com.merit.api.categories.CategoryRoutes
import com.merit.api.inventoryCount.InventoryCountRoutes
import com.merit.modules.inventoryCount.InventoryCountService
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.RejectionHandler
import com.merit.api.stats.StatsRoutes
import com.merit.modules.statsService.StatsService
import com.merit.api.events.SaleEventsRoute
import com.merit.modules.salesEvents.SaleEventService
import com.typesafe.scalalogging.LazyLogging
import com.merit.api.inventoryCount.SyncInventoryCount

object Router extends Directives with AuthDirectives with JsonSupport with LazyLogging {
  def apply(
    saleService: SaleService,
    productService: ProductService,
    brandService: BrandService,
    excelService: ExcelService,
    userService: UserService,
    categoryService: CategoryService,
    stockOrderService: StockOrderService,
    inventoryCountService: InventoryCountService,
    statsService: StatsService,
    saleEventService: SaleEventService,
    config: AppConfig
  )(implicit ec: ExecutionContext): Route = {
    def crawlerAuthenticator(credentials: Credentials): Option[Boolean] =
      credentials match {
        case c @ Credentials.Provided(config.crawlerClientConfig.username)
            if c.verify(config.crawlerClientConfig.password) =>
          Some(true)
        case _ => None
      }

    val rejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

    val exceptionHandler = ExceptionHandler {
      case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
      case t: Throwable =>
        complete {
          logger.error("An unhandled error occurred: ", t)
          StatusCodes.InternalServerError -> "Something went wrong"
        }
    }

    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

    cors() {
      handleErrors {
        LoginRoute(userService, config.jwtConfig) ~
        SyncSaleRoute(saleService, crawlerAuthenticator) ~
        SyncStockOrderRoute(stockOrderService, crawlerAuthenticator) ~
        SyncInventoryCount(inventoryCountService, crawlerAuthenticator) ~
        authenticated(config.jwtConfig) { userId =>
          ProductRoutes(productService, excelService) ~
          SaleRoutes(saleService, excelService, userId) ~
          StockOrderRoutes(stockOrderService, excelService) ~
          BrandRoutes(brandService) ~
          CategoryRoutes(categoryService) ~
          InventoryCountRoutes(inventoryCountService, excelService) ~
          UserRoutes(userId, userService) ~
          StatsRoutes(statsService) ~
          SaleEventsRoute(saleEventService)
        }
      }
    }
  }
}
