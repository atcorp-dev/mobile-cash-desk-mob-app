// IPaymentService.aidl
package ua.com.atcorp.mobilecashdesk;
import ua.com.atcorp.mobilecashdesk.IPLAmountRecalculationProvider;

interface IPaymentService {

    Map updateSettings();

    Map cutover();

    Map signOn();

    Map reversal(String referenceNumber, String transactionNumber, String invoiceNumber);

    Map payment(double amount, String urlForAmountRecalculation, String ref_1, String ref_2, String ref_3, String ref_4, String customer_email, String mobile, String source, String extra);

    Map paymentWithRecalculationProvider(double amount, IPLAmountRecalculationProvider amountRecalculationProvider, String ref_1, String ref_2, String ref_3, String ref_4, String customer_email, String mobile, String source, String extra);

    void cancelCurrentTransaction();
}
