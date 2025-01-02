import org.junit.jupiter.api.Test;
import ru.gafarov.betservice.utils.CryptoUtils;


public class CryptoTest {

    @Test
    void test(){
//        System.out.println(CryptoUtils.encrypt("12334444c05b4e36d2ac7269dd8", "123456"));
//        System.out.println(CryptoUtils.decrypt("cyY3Z+glsuL4p7rCkxJIn/vx/mMl9Yz4IUgjoHorVGgHHNi7rtoRSZB0pfRf58AC", "123456"));
        System.out.println(CryptoUtils.decrypt("zc+9tiJ48PW7aWykwL4zSHXk27+VE6eyfzw2aXixZ4c=", "123456"));
        for (int i = 0; i < 123456; i++) {
            try {
            String m = CryptoUtils.decrypt("ZUs6JQ6S+UVotaKAl2zGXhNMBMWf4iZ3ABzXriMtlag=", String.valueOf(i));
                System.out.println(i + " " + m);
            } catch (RuntimeException e) {

            }
        }
    }
}
