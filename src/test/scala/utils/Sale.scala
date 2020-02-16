package utils

import org.joda.time.DateTime
import utils.ProductUtils._
import utils.ExcelTestUtils._
import com.merit.modules.products.{Currency, ProductID}
import com.merit.modules.sales.{SaleOutlet, SaleStatus, SaleDTOProduct, SaleID, SaleDTO}

object SaleUtils {
  def createSales(count: Int): Seq[SaleDTO] =
    (0 to count)
      .map(
        i =>
          SaleDTO(
            SaleID(i),
            DateTime.now(),
            SaleOutlet.Store,
            SaleStatus.SaleCompleted,
            Currency(randomBetween(10000)),
            Currency(randomBetween(1000)),
            getExcelProductRows(5).map(excelRowToSaleDTOProduct(_))
          )
      )
      .toSeq

  def sortedWithZeroId(products: Seq[SaleDTOProduct]): Seq[SaleDTOProduct] =
    products.map(_.copy(id = ProductID.zero)).sortBy(_.barcode)
}
