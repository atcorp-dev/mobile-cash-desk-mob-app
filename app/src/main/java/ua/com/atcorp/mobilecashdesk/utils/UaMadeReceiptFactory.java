package ua.com.atcorp.mobilecashdesk.utils;

import android.content.Context;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ua.com.atcorp.mobilecashdesk.models.Cart;
import ua.com.atcorp.mobilecashdesk.models.CartItem;
import ua.com.atcorp.mobilecashdesk.rest.dto.TransactionDto;
import ua.com.atcorp.mobilecashdesk.services.AuthService;

public class UaMadeReceiptFactory {

    private AuthService mAuthService;

    public UaMadeReceiptFactory(Context context) {
        mAuthService = new AuthService(context);
    }

    public String getReceipt(Cart cart, TransactionDto transactionDto) {
        DecimalFormat df = new DecimalFormat("0.00");
        String currentDateStr = new SimpleDateFormat("dd.MM.yy").format(new Date());
        String currentTimeStr = new SimpleDateFormat("HH:mm:ss").format(new Date());
        ArrayList<String> hb = new ArrayList<>();
        hb.add("<html>");
        hb.add("<body>");
        hb.add("<div style=\"width:380px\">");
        // region IMG
        hb.add("<img style=\"margin-left:80px; width:300px\" src=\"data:image/png;base64,");
        hb.add("iVBORw0KGgoAAAANSUhEUgAAASwAAABHAQMAAACH9+gbAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8");
        hb.add("YQUAAAAGUExURQAAAP///6XZn90AAAAJcEhZcwAAEnQAABJ0Ad5mH3gAAAFLaVRYdFhNTDpjb20uYWRv");
        hb.add("YmUueG1wAAAAAAA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5");
        hb.add("ZCI/Pgo8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAg");
        hb.add("Q29yZSA1LjYtYzEzOCA3OS4xNTk4MjQsIDIwMTYvMDkvMTQtMDE6MDk6MDEgICAgICAgICI+CiA8cmRm");
        hb.add("OlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMi");
        hb.add("PgogIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiLz4KIDwvcmRmOlJERj4KPC94OnhtcG1ldGE+");
        hb.add("Cjw/eHBhY2tldCBlbmQ9InIiPz4gSa46AAADf0lEQVRIx83WvY7USBAH8OqxtXawmrYgPWhLew+wIZmH");
        hb.add("iMvgEYiICPYF7tyDCIiOfYCT4BHIIMNiE0LuCXBAgE5I58u84HXdv6rt8Qyzo3NwAQ5Wszu/6equj94h");
        hb.add("nvM0Pyq7mse6maz8P9lVMW+1eezSzWLfXDSL+ZP/Yh+FXSQNvdBfrzL368W+qskyX7BtyBOdUtQSUbTP");
        hb.add("KrDWdk7ZGZnuEEv4Hz4ulDUUfT3M2vJLqaxG0PRg0M79MbHDe2N7C+ldjyw2B46QdLy1Wnr3GuYt149+");
        hb.add("4RlB6dHxDNYZe2frCGl8DevItun9N2CXlPQk/UnFtazJ3xPSi1cDK/dZT/ZinT2cWH2AHS2IWrfLWhQX");
        hb.add("PxOARrbQ3MbJUjI7rBBWWfaSZy/WH+UkT7lh1SpXRlFPJNHE0mKlbFqtopUDw9OG8GrxmIxw3JEhfcpy");
        hb.add("NCm5kB/JOS0QN5lYHN6sACGqwdIiRtxoYkcoX4O9yVPI4omyOMVLs2G0VBaHLfda4lrS8ScYfc/SbMOM");
        hb.add("RKboNe0wKx1ImqfU8ch88omiGkM1MofNyLseaXdyIoMNGrJ/oUk2rKdM2Fq3JuFLLyyXlLluYisyYE9x");
        hb.add("euMlPFeGJdW2FnY6MBnnmyii6SnpZJy4BsOUgxX9xDx5dK+/y9KlcnE0YPjEfTnu9moZ2DNsSFlPZ0Y/");
        hb.add("8VxZNrF8zfxEGGtqMonfm+d+m6HKxsjeBoY6KVvYZ+HwG0Yr5nPi84RlhpXZPrY+sH5kKTrMk6wGVg0s");
        hb.add("dVkIGpg0A+neKmV+YJl9sMfQTiOjgeXLnweWB4Y4Ja93WdJVy8ceM/aK+tXAUBV+OjAIFCRNOn/7i8xv");
        hb.add("E4JKaYShEgOzlekzVAGsCCzp48AKfrlhTmqKG+inDn8uAqOR+ZMNkw4xTS6sT4RFHUnboLHwea5O+FwY");
        hb.add("Do1WTo/70G/4TLvC7YkyYrilswvdmyH3RDJRSveegqHoHbq5IoMSWWH0mw6Ol+kuca9Aa4rQIEvKMY47");
        hb.add("THuoDOkVJlfvUudsYvHIKm1LKZ8ys8V0CYc9rALDEd7hHmgXtFzssVDoWsNIa9nvg5bSZa4drkC5avSO");
        hb.add("usyUnW2YRHIybjekFZFeucXKS6LfB/ZBElHU9FCyjZtHxgThMUQR7wTFCcvPRjqhcsi3XLLylaCxcnlY");
        hb.add("onv81n+sIv5bL/t2/Adw9QN/G2Tm5l+bACTWZsBBrQAAAABJRU5ErkJggg==");
        hb.add("\" />");
        // endregion
        hb.add("<font face=\"Arial\" size=\"3\"><strong>UAMadeStore</strong></font>");
        hb.add("<table>");
        hb.add("<tbody>");
        hb.add("<tr><td colspan=\"4\"><div><font face=\"Arial\" size=\"3\">г. Киев ул. Татарская 7</font></div></td></tr>");
        hb.add(
                String.format(
                        "<tr colspan=\"4\"><td><div><font face=\"Arial\" size=\"3\">касир:%s</font></div></td></tr>",
                        mAuthService.getCurrentUser().getName())
        );
        hb.add("<tr><td colspan=\"4\"><br /><td></tr>");
        hb.add("<tr><td colspan=\"4\"><div><font face=\"Arial\" size=\"3\">email: info@uamade.com.ua</font></div></td></tr>");
        hb.add("<tr><td colspan=\"4\"><div><font face=\"Arial\" size=\"3\">http://uamade.com.ua/</font></div></tr>");
        hb.add(
                String.format(
                        "<tr><td colspan=\"4\"><div><font face=\"Arial\" size=\"3\">чек №МК%s-%s</font></div></td></tr>",
                        mAuthService.getCurrentCompany().getCode(),
                        transactionDto.documentNumber)
        );
        hb.add("<tr><td colspan=\"4\"><br /></td></tr>");
        hb.add(
                String.format(
                        "<tr><td colspan=\"4\"><div><font face=\"Arial\" size=\"3\">номер заказа: %s </font></div></td></tr>",
                        transactionDto.documentNumber)
        );
        for(CartItem item : cart.getItems()) {
            hb.add("<tr>");
            hb.add("<td>");
            hb.add(
                    String.format(
                            "<div><font face=\"Arial\" size=\"2\">%s</font></div>",
                            item.getItemName())
            );
            String itemPrice = df.format(item.getItemPrice() - item.getDiscount());
            String price = df.format(item.getPrice());
            hb.add(
                    String.format(
                            "<div><font face=\"Arial\" size=\"3\">%sшт.*%s грн.      =      %s</font></div>",
                            item.getQty(),
                            itemPrice,
                            price
                            )
            );
            hb.add("</td>");
            hb.add("</tr>");
        }
        hb.add("<tr>");
        hb.add("<td width=\"25%\"></td>");
        hb.add("<td width=\"25%\"><font face=\"Arial\" size=\"3\"><strong>Сума:</strong></font></td>");
        hb.add("<td width=\"25%\"></td>");
        String totalPrice = df.format(cart.getTotalPrice());
        hb.add("<td width=\"25%\"><font face=\"Arial\" size=\"4\">" + totalPrice + "</font></td>");
        hb.add("</tr>");
        hb.add("<tr>");
        hb.add("<td width=\"25%\"><font face=\"Arial\" size=\"1\">" + currentDateStr + "</font></td>");
        hb.add("<td width=\"25%\"></td>");
        hb.add("<td width=\"25%\"><font face=\"Arial\" size=\"1\">" + currentTimeStr + "</font></td>");
        hb.add("<td width=\"25%\"></td>");
        hb.add("</tr>");
        hb.add("<tr>");
        hb.add("<td width=\"25%\"></td>");
        hb.add("<td width=\"75%\" colspan=3><font face=\"Arial\" size=\"3\"><strong>Дякуємо за покупку!</strong></font></td>");
        hb.add("<tr>");
        hb.add("<tr><td colspan=4 align=\"absMiddle\"><font face=\"Arial\" size=\"2\">Побажання від UAmade:</font></td></tr>");
        hb.add("<tr><td colspan=4><font face=\"Arial\" size=\"2\">Даруй всім свою усмішку</font></td></tr>");
        hb.add("<tr><td colspan=4 align=\"absMiddle\"><font face=\"Arial\" size=\"3\"><strong>uamade.com.ua</strong></font></td></tr>");
        hb.add("</tbody>");
        hb.add("</table>");
        hb.add("</body>");
        hb.add("</html>");
        String receipt = "";
        for( String line : hb)
            receipt += line + "\n";

        return receipt;
    }
}
