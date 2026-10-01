package dto;

import java.io.Serializable;

public class CustomerSource implements Serializable {

    private static final long serialVersionUID = 1L;

    private String country;
    private long total;

    public CustomerSource() {
    }

    public CustomerSource(
            String country,
            long total) {

        this.country = country;
        this.total = total;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "CustomerSource[country="
                + country
                + ", total="
                + total
                + "]";
    }
}