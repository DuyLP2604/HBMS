package util;

import java.sql.Connection;

@FunctionalInterface
public interface CounterPaymentOperation<T> {
    T run(Connection connection) throws Exception;
}
