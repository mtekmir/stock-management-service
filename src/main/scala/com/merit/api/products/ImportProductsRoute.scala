package api

import com.merit.modules.brands.BrandService
import com.merit.modules.products.ProductService
import scala.concurrent.Await
import akka.http.scaladsl.server.Directives
import scala.concurrent.duration.Duration
import scala.util.Success
import com.merit.modules.excel.ExcelService
import scala.concurrent.ExecutionContext
import com.merit.modules.categories.CategoryService
import akka.http.scaladsl.model.StatusCodes.BadRequest

object ImportRoute extends Directives with JsonSupport {
  def apply(
    productService: ProductService,
    excelService: ExcelService
  )(implicit ec: ExecutionContext) =
    (path("import") & uploadedFile("file")) {
      case (metadata, file) => {
        val rows       = excelService.parseProductImportFile(file)
        
        rows match {
          case Left(error) => complete(BadRequest -> error)
          case Right(rows) => complete(productService.batchInsertExcelRows(rows))
        }
      }
    }
}
