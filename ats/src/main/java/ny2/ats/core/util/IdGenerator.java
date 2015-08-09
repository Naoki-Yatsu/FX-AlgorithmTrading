package ny2.ats.core.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private AtomicInteger atomicInteger = new AtomicInteger();

    private DecimalFormat format4Number = new DecimalFormat("0000");

    private DecimalFormat format6Number = new DecimalFormat("000000");

    private DecimalFormat format8Number = new DecimalFormat("00000000");

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMddHHmm");

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYYMMdd");

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public IdGenerator() {
        initFormat();
    }

    public IdGenerator(int start) {
        initFormat();
        atomicInteger.set(start);
    }

    private void initFormat() {
        format4Number.setMaximumIntegerDigits(4);
        format6Number.setMaximumIntegerDigits(6);
        format8Number.setMaximumIntegerDigits(8);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * YYYYMMddHHmm + 4桁の数字 のIDを返します。
     * @return
     */
    public String createIdDateTime4Number() {
        return  createYYYYMMddHHmm() + create4number();
    }

    /**
     * YYYYMMddHHmm + 6桁の数字 のIDを返します。
     * @return
     */
    public String createIdDateTime6Number() {
        return  createYYYYMMddHHmm() + create6number();
    }

    /**
     * YYYYMMddHHmm + 6桁の数字 のIDを返します。
     * @return
     */
    public long createLongIdDateTime6Number() {
        return  Long.parseLong(createIdDateTime6Number());
    }

    /**
     * YYYYMMdd + 8桁の数字 のIDを返します。
     * @return
     */
    public String createIdDate8Number() {
        return  createYYYYMMdd() + create8number();
    }

    /**
     * 4桁の数字を返します。
     * @return
     */
    public String create4number() {
        return format4Number.format(atomicInteger.getAndIncrement());
    }

    /**
     * 6桁の数字を返します。
     * @return
     */
    public String create6number() {
        return format6Number.format(atomicInteger.getAndIncrement());
    }

    /**
     * 8桁の数字を返します。
     * @return
     */
    public String create8number() {
        return format8Number.format(atomicInteger.getAndIncrement());
    }

    /**
     * YYYYMMddHHmm を返します。
     * @return
     */
    public String createYYYYMMddHHmm() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * YYYYMMdd を返します。
     * @return
     */
    public String createYYYYMMdd() {
        LocalDate localDate = LocalDate.now();
        return localDate.format(dateFormatter);
    }

}
