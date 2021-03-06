package api

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.AutoDerivation
import org.joda.time.DateTime
import io.circe.Json
import io.circe.Encoder
import io.circe.Decoder
import com.merit.modules.sales.{SaleID, SaleOutlet}
import com.merit.modules.products.ProductID
import com.merit.modules.brands.BrandID
import com.merit.modules.users.UserID
import com.merit.modules.excel.ValidationErrorTypes

import org.joda.time.format.DateTimeFormat
import com.merit.modules.products.Currency
import cats.syntax.either._
import com.merit.modules.stockOrders.StockOrderID
import com.merit.modules.inventoryCount.{InventoryCountBatchID, InventoryCountProductID, InventoryCountStatus}
import org.joda.time.format.ISODateTimeFormat
import com.merit.modules.categories.CategoryID
import com.merit.modules.salesEvents.SaleEventType
import com.merit.modules.salesEvents.SaleEventID
import com.merit.modules.sales.SaleStatus
import com.merit.modules.sales.PaymentMethod

trait JsonSupport extends FailFastCirceSupport with AutoDerivation {
  implicit val encodeDT: Encoder[DateTime] = (d: DateTime) =>
    Encoder.encodeString(d.toString())

  implicit val decodeIso: Decoder[DateTime] =
    Decoder.instance(d => d.as[String].map(s => DateTime.parse(s)))

  implicit val encodeSaleId: Encoder[SaleID] = (id: SaleID) => Encoder.encodeLong(id.value)
  implicit val decodeSaleId: Decoder[SaleID] = Decoder.decodeLong.emap { v =>
    SaleID(v).asRight
  }
  implicit val encodeStockOrderId: Encoder[StockOrderID] = (id: StockOrderID) =>
    Encoder.encodeLong(id.value)
  implicit val decodeStockOrderId: Decoder[StockOrderID] = Decoder.decodeLong.emap { v =>
    StockOrderID(v).asRight
  }

  implicit val encodeProductId: Encoder[ProductID] = (id: ProductID) =>
    Encoder.encodeLong(id.value)
  implicit val decodeProductId: Decoder[ProductID] = Decoder.decodeLong.emap { v =>
    ProductID(v).asRight
  }

  implicit val encodeBrandId: Encoder[BrandID] = (id: BrandID) => Encoder.encodeLong(id.value)
  implicit val decodeBrandId: Decoder[BrandID] = Decoder.decodeLong.emap { v =>
    BrandID(v).asRight
  }

  implicit val encodeCategoryId: Encoder[CategoryID] = (id: CategoryID) =>
    Encoder.encodeLong(id.value)
  implicit val decodeCategoryId: Decoder[CategoryID] = Decoder.decodeLong.emap { v =>
    CategoryID(v).asRight
  }

  implicit val encodeUserId: Encoder[UserID] = (id: UserID) => Encoder.encodeUUID(id.value)
  implicit val decodeUserId: Decoder[UserID] = Decoder.decodeUUID.emap { v =>
    UserID(v).asRight
  }

  implicit val encodeSaleEventId: Encoder[SaleEventID] = (id: SaleEventID) =>
    Encoder.encodeLong(id.value)
  implicit val decodeSaleEventId: Decoder[SaleEventID] = Decoder.decodeLong.emap { v =>
    SaleEventID(v).asRight
  }

  implicit val encodeInventoryCountBatchId: Encoder[InventoryCountBatchID] =
    (id: InventoryCountBatchID) => Encoder.encodeLong(id.value)
  implicit val decodeInventoryCountBatchId: Decoder[InventoryCountBatchID] =
    Decoder.decodeLong.emap { v =>
      InventoryCountBatchID(v).asRight
    }

  implicit val encodeInventoryCountStatus: Encoder[InventoryCountStatus] =
    (s: InventoryCountStatus) => Encoder.encodeString(InventoryCountStatus.toString(s))
  implicit val decodeInventoryCountStatus: Decoder[InventoryCountStatus] =
    Decoder.decodeString.emap { v =>
      InventoryCountStatus.fromString(v).asRight
    }

  implicit val encodeInventoryCountProductId: Encoder[InventoryCountProductID] =
    (id: InventoryCountProductID) => Encoder.encodeLong(id.value)
  implicit val decodeInventoryCountProductId: Decoder[InventoryCountProductID] =
    Decoder.decodeLong.emap { v =>
      InventoryCountProductID(v).asRight
    }

  implicit val encodeCurrency: Encoder[Currency] = (currency: Currency) =>
    Encoder.encodeBigDecimal(currency.value)

  implicit val decodeCurrency: Decoder[Currency] = Decoder.decodeBigDecimal.emap { v =>
    Currency(v).asRight
  }

  implicit val encodeErrorType: Encoder[ValidationErrorTypes.Value] =
    Encoder.enumEncoder(ValidationErrorTypes)

  implicit val encodeSaleOutlet: Encoder[SaleOutlet.Value] =
    Encoder.enumEncoder(SaleOutlet)

  implicit val decodeSaleOutlet: Decoder[SaleOutlet.Value] =
    Decoder.enumDecoder(SaleOutlet)

  implicit val encodeSaleStatus: Encoder[SaleStatus.Value] =
    Encoder.enumEncoder(SaleStatus)

  implicit val decodeSaleStatus: Decoder[SaleStatus.Value] =
    Decoder.enumDecoder(SaleStatus)

  implicit val encodeSaleEvent: Encoder[SaleEventType.Value] =
    Encoder.enumEncoder(SaleEventType)

  implicit val encodePaymentMethod: Encoder[PaymentMethod.Value] =
    Encoder.enumEncoder(PaymentMethod)

  implicit val decodePaymentMethod: Decoder[PaymentMethod.Value] =
    Decoder.enumDecoder(PaymentMethod)
}
