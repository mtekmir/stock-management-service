package com.merit.modules.sales

import slick.lifted.MappedTo
import java.sql.Timestamp
import com.merit.modules.products._
import org.joda.time.DateTime
import com.merit.modules.brands.BrandRow
import com.merit.modules.categories.CategoryRow

case class SaleID(value: Long) extends AnyVal with MappedTo[Long]

object SaleOutlet extends Enumeration {
  type SaleOutlet = Value
  val Store = Value("Store")
  val Web   = Value("Web")
}

object SaleStatus extends Enumeration {
  type SaleStatus = Value

  val OrderPlaced              = Value("Order Placed")
  val OrderAwaitingShipment    = Value("Order Awaiting Shipment")
  val OrderShipped             = Value("Order Shipped")
  val OrderRefunded            = Value("Order Refunded")
  val OrderReturned            = Value("Order Returned")
  val OrderPickedUp            = Value("Order Picked Up")
  val OrderReturnRequested     = Value("Order Return Requested")
  val OrderCancelled           = Value("Order Cancelled")
  val OrderAwaitingPayment     = Value("Order Awaiting Payment")
  val OrderPartiallyRefunded   = Value("Order Partially Refunded")
  val OrderAccepted            = Value("Order Accepted")
  val OrderAwaitingFulfillment = Value("Order Awaiting Fulfillment")

  val SaleCompleted         = Value("Sale Completed")
  val SaleReturned          = Value("Sale Returned")
  val SalePartiallyReturned = Value("Sale Partially Returned")

  val fulfilledSaleStatuses =
    List(OrderShipped, OrderPickedUp, OrderAwaitingShipment, SaleCompleted)
  val trStatuses = Map(
    "Sipariş Alındı"       -> OrderPlaced,
    "Paketleniyor"         -> OrderAwaitingShipment,
    "Kargoya Verildi"      -> OrderShipped,
    "İade Ödemesi Yapıldı" -> OrderRefunded,
    "İade Edildi"          -> OrderReturned,
    "Teslim Edildi"        -> OrderPickedUp,
    "İade Talebi Alındı"   -> OrderReturnRequested,
    "İptal Edildi"         -> OrderCancelled,
    "Ödeme Bekliyor"       -> OrderAwaitingShipment,
    "Kısmi İade Yapıldı"   -> OrderPartiallyRefunded,
    "Onaylandı"            -> OrderAccepted,
    "Tedarik Ediliyor"     -> OrderAwaitingFulfillment
  )

  def isValid(s: String): Boolean                 = trStatuses.contains(s)
  def parseFromExcel(s: String): SaleStatus.Value = trStatuses.get(s).getOrElse(SaleCompleted)
}

object PaymentMethod extends Enumeration {
  type PaymentMethod = Value
  val Cash       = Value("Cash")
  val CreditCard = Value("CreditCard")
  val OnCredit   = Value("OnCredit")

  val trPaymentMethods = Map(
    "IyziPay" -> CreditCard,
    "Havale"  -> Cash
  )

  def parseFromExcel(s: String) = trPaymentMethods.get(s).getOrElse(CreditCard)
}

case class SaleRow(
  createdAt: DateTime = DateTime.now(),
  total: Currency,
  discount: Currency = Currency(0),
  outlet: SaleOutlet.Value = SaleOutlet.Store,
  status: SaleStatus.Value = SaleStatus.SaleCompleted,
  orderNo: Option[String] = None,
  description: Option[String] = None,
  paymentMethod: PaymentMethod.Value = PaymentMethod.Cash,
  id: SaleID = SaleID(0L)
)

case class SaleDTO(
  id: SaleID,
  createdAt: DateTime,
  outlet: SaleOutlet.Value,
  status: SaleStatus.Value,
  orderNo: Option[String],
  total: Currency,
  discount: Currency,
  description: Option[String],
  paymentMethod: PaymentMethod.Value,
  products: Seq[SaleDTOProduct]
)

case class SaleDTOProduct(
  id: ProductID,
  barcode: String,
  sku: String,
  name: String,
  price: Currency,
  discountPrice: Option[Currency],
  qty: Int,
  variation: Option[String],
  taxRate: Option[Int],
  brand: Option[String],
  category: Option[String],
  synced: Boolean = false
)

object SaleDTOProduct {
  def fromRow(
    productRow: ProductRow,
    brand: Option[BrandRow] = None,
    category: Option[CategoryRow] = None,
    synced: Boolean,
    soldQty: Int
  ): SaleDTOProduct = {
    import productRow._
    SaleDTOProduct(
      id,
      barcode,
      sku,
      name,
      price,
      discountPrice,
      soldQty,
      variation,
      taxRate,
      brand.map(_.name),
      category.map(_.name),
      synced
    )
  }
}

case class SaleSummaryProduct(
  id: ProductID,
  barcode: String,
  name: String,
  variation: Option[String] = None,
  prevQty: Int,
  soldQty: Int
)

object SaleSummaryProduct {
  def fromProductDTO(p: ProductDTO, soldQty: Int): SaleSummaryProduct = {
    import p._
    SaleSummaryProduct(id, barcode, name, variation, qty, soldQty)
  }
}

case class SaleSummary(
  id: SaleID,
  createdAt: DateTime,
  total: Currency,
  discount: Currency,
  outlet: SaleOutlet.Value,
  status: SaleStatus.Value,
  description: Option[String],
  paymentMethod: PaymentMethod.Value,
  products: Seq[SaleSummaryProduct]
)

case class SaleFilters(
  startDate: Option[DateTime] = None,
  endDate: Option[DateTime] = None
)

case class PaginatedSalesResponse(
  count: Int,
  sales: Seq[SaleDTO]
)

case class WebSaleRow(
  orderNo: String,
  total: Currency,
  discount: Currency,
  createdAt: DateTime,
  status: SaleStatus.Value,
  paymentMethod: PaymentMethod.Value,
  products: Seq[WebSaleRowProduct]
)

case class WebSaleRowProduct(
  name: String,
  sku: Option[String],
  brand: String,
  barcode: Option[String],
  qty: Int,
  price: Currency,
  tax: Int
)

case class WebSaleSummary(
  orderNo: String,
  total: Currency,
  discount: Currency,
  createdAt: DateTime,
  status: SaleStatus.Value,
  paymentMethod: PaymentMethod.Value,
  products: Seq[WebSaleSummaryProduct]
)

case class WebSaleSummaryProduct(
  sku: String,
  barcode: String,
  qty: Int
)
