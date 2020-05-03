// IPLAmountRecalculationProvider.aidl
package ua.com.atcorp.mobilecashdesk;

interface IPLAmountRecalculationProvider {

    double getAmountForOperationWithCardHash(String hash);
}
