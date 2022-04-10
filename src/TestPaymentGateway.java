import java.util.HashMap;

public class TestPaymentGateway
{
    public static void main(String arg[])
    {
        HashMap retval = new HashMap();
        PaymentGateway gw = new PaymentGateway("6457Thfj624V5r7WUwc5v6a68Zsd6YEm");

        try {
        	double num = 10.00;
            retval = gw.doSale(num, "5431111111111111", "0909");
            System.out.println("Success\nTransId: " + retval.get("transactionid") + "\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

    }
}
