package db

import slick.lifted._
import com.merit.modules.products.{ProductID, ProductRow, SoldProductRow, OrderedProductRow}
import com.merit.modules.brands.{BrandID, BrandRow}
import slick.driver.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import java.sql.Timestamp

import com.merit.modules.stockOrders.{StockOrderID, StockOrderRow}
import org.joda.time.DateTime
import com.merit.modules.sales.{SaleID, SaleRow}
import com.merit.modules.users.{UserID, UserRow}

class Schema(val profile: JdbcProfile) {
  import profile.api._

  object CustomColumnTypes {
    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime)
      )
  }

  class ProductTable(t: Tag) extends Table[ProductRow](t, "products") {
    def id        = column[ProductID]("id", O.PrimaryKey, O.AutoInc)
    def barcode   = column[String]("barcode")
    def sku       = column[String]("sku")
    def name      = column[String]("name")
    def price     = column[Double]("price")
    def qty       = column[Int]("qty")
    def variation = column[String]("variation")
    def brandId   = column[BrandID]("brandId")

    def * = (barcode, sku, name, price, qty, variation, brandId, id).mapTo[ProductRow]

    def brand =
      foreignKey("brand_pk", brandId, brands)(_.id, onDelete = ForeignKeyAction.SetNull)
  }

  lazy val products = TableQuery[ProductTable]

  class BrandTable(t: Tag) extends Table[BrandRow](t, "brands") {
    def id   = column[BrandID]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.Unique)

    def * = (name, id).mapTo[BrandRow]
  }

  lazy val brands = TableQuery[BrandTable]

  class SaleTable(t: Tag) extends Table[SaleRow](t, "sales") {
    import CustomColumnTypes._
    def id        = column[SaleID]("id", O.PrimaryKey, O.AutoInc)
    def createdAt = column[DateTime]("created")

    def * = (createdAt, id).mapTo[SaleRow]
  }

  lazy val sales = TableQuery[SaleTable]

  class SoldProductTable(t: Tag) extends Table[SoldProductRow](t, "sold_products") {
    def id        = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def productId = column[ProductID]("productId")
    def saleId    = column[SaleID]("saleId")
    def qty       = column[Int]("qty")

    def productFk = foreignKey("product_fk", productId, products)(_.id)
    def saleFk    = foreignKey("sale_fk", saleId, sales)(_.id)

    def * = (productId, saleId, qty, id).mapTo[SoldProductRow]
  }

  lazy val soldProducts = TableQuery[SoldProductTable]

  class UserTable(t: Tag) extends Table[UserRow](t, "users") {
    def id       = column[UserID]("id", O.PrimaryKey)
    def email    = column[String]("email")
    def name     = column[String]("name")
    def password = column[String]("password")

    def * = (email, name, password, id).mapTo[UserRow]
  }

  lazy val users = TableQuery[UserTable]

  class StockOrderTable(t: Tag) extends Table[StockOrderRow](t, "stock_orders") {
    import CustomColumnTypes._
    def id      = column[StockOrderID]("id", O.PrimaryKey, O.AutoInc)
    def created = column[DateTime]("created")

    def * = (created, id).mapTo[StockOrderRow]
  }

  lazy val stockOrders = TableQuery[StockOrderTable]

  class OrderedProductsTable(t: Tag) extends Table[OrderedProductRow](t, "ordered_products") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def productId = column[ProductID]("productId")
    def stockOrderId = column[StockOrderID]("stockOrderId")
    def qty = column[Int]("qty")

    def productFk = foreignKey("product_fk", productId, products)(_.id)
    def stockOrderFk = foreignKey("stock_order_fk", stockOrderId, stockOrders)(_.id)

    def * = (productId, stockOrderId, qty, id).mapTo[OrderedProductRow]
  }

  lazy val orderedProducts = TableQuery[OrderedProductsTable]

  def createTables(db: Database)(implicit ec: ExecutionContext): Vector[Unit] = {
    val tables   = Vector(brands, products, sales, soldProducts, users, stockOrders, orderedProducts)
    val existing = db.run(MTable.getTables)

    val f = existing.flatMap(ts => {
      val existingTableNames = ts.map(_.name.name)
      val createIfNotExists = tables
        .filterNot(t => existingTableNames.contains(t.baseTableRow.tableName))
        .map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExists))
    })

    Await.result(f, Duration.Inf)
  }
}

object Schema {
  def apply(profile: JdbcProfile) = new Schema(profile)
}
