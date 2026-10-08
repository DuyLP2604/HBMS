package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

public class PersistenceManager
{
    private static final String UNIT_NAME = "my_persistence_unit";
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory(UNIT_NAME);

    public static EntityManager createEntityManager()
    {
        return emf.createEntityManager();
    }

    public static EntityManagerFactory getEMF()
    {
        return emf;
    }

    public static Connection openConnection() throws SQLException
    {
        Map<String, Object> settings = readConnectionSettings();
        Object source = setting(settings, "jakarta.persistence.nonJtaDataSource", "javax.persistence.nonJtaDataSource");
        Connection connection;
        if (source != null)
        {
            connection = resolveDataSource(source).getConnection();
        }
        else
        {
            Object url = setting(settings, "jakarta.persistence.jdbc.url", "javax.persistence.jdbc.url");
            if (url == null || url.toString().isBlank())
            {
                throw new SQLException("No JDBC URL or non-JTA DataSource found for " + UNIT_NAME + ". Check META-INF/persistence.xml.");
            }
            Object driver = setting(settings, "jakarta.persistence.jdbc.driver", "javax.persistence.jdbc.driver");
            if (driver != null)
            {
                try
                {
                    Class.forName(driver.toString());
                }
                catch (ClassNotFoundException exception)
                {
                    throw new SQLException("The configured JDBC driver is unavailable.", exception);
                }
            }
            Properties credentials = new Properties();
            Object user = setting(settings, "jakarta.persistence.jdbc.user", "javax.persistence.jdbc.user");
            Object password = setting(settings, "jakarta.persistence.jdbc.password", "javax.persistence.jdbc.password");
            if (user != null)
            {
                credentials.setProperty("user", user.toString());
            }
            if (password != null)
            {
                credentials.setProperty("password", password.toString());
            }
            connection = DriverManager.getConnection(url.toString(), credentials);
        }
        try
        {
            if (!connection.getAutoCommit())
            {
                connection.rollback();
                connection.setAutoCommit(true);
            }
            try (Statement statement = connection.createStatement())
            {
                statement.execute("SET IMPLICIT_TRANSACTIONS OFF; SET ANSI_NULLS ON; SET ANSI_PADDING ON; SET ANSI_WARNINGS ON; SET ARITHABORT ON; SET CONCAT_NULL_YIELDS_NULL ON; SET QUOTED_IDENTIFIER ON; SET NUMERIC_ROUNDABORT OFF; SET NOCOUNT ON;");
            }
            return connection;
        }
        catch (SQLException exception)
        {
            try
            {
                connection.close();
            }
            catch (SQLException closeException)
            {
                exception.addSuppressed(closeException);
            }
            throw exception;
        }
    }

    private static Map<String, Object> readConnectionSettings() throws SQLException
    {
        Map<String, Object> settings = new HashMap<>();
        try (InputStream input = PersistenceManager.class.getClassLoader().getResourceAsStream("META-INF/persistence.xml"))
        {
            if (input != null)
            {
                XMLInputFactory factory = XMLInputFactory.newFactory();
                factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
                factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);
                XMLStreamReader reader = factory.createXMLStreamReader(input);
                try
                {
                    boolean selected = false;
                    while (reader.hasNext())
                    {
                        int event = reader.next();
                        if (event == XMLStreamConstants.START_ELEMENT)
                        {
                            String name = reader.getLocalName();
                            if ("persistence-unit".equals(name))
                            {
                                selected = UNIT_NAME.equals(reader.getAttributeValue(null, "name"));
                            }
                            else if (selected && "property".equals(name))
                            {
                                String key = reader.getAttributeValue(null, "name");
                                String value = reader.getAttributeValue(null, "value");
                                if (key != null && value != null)
                                {
                                    settings.put(key, value);
                                }
                            }
                            else if (selected && "non-jta-data-source".equals(name))
                            {
                                settings.put("jakarta.persistence.nonJtaDataSource", reader.getElementText().trim());
                            }
                        }
                        else if (event == XMLStreamConstants.END_ELEMENT && "persistence-unit".equals(reader.getLocalName()))
                        {
                            selected = false;
                        }
                    }
                }
                finally
                {
                    reader.close();
                }
            }
        }
        catch (Exception exception)
        {
            throw new SQLException("Unable to read JDBC settings from META-INF/persistence.xml.", exception);
        }
        for (Map.Entry<String, Object> entry : emf.getProperties().entrySet())
        {
            if (entry.getValue() != null)
            {
                settings.put(entry.getKey(), entry.getValue());
            }
        }
        return settings;
    }

    private static Object setting(Map<String, Object> settings, String jakartaName, String legacyName)
    {
        Object value = settings.get(jakartaName);
        return value == null ? settings.get(legacyName) : value;
    }

    private static DataSource resolveDataSource(Object source) throws SQLException
    {
        if (source instanceof DataSource)
        {
            return (DataSource) source;
        }
        try
        {
            InitialContext context = new InitialContext();
            try
            {
                Object value = context.lookup(source.toString());
                if (!(value instanceof DataSource))
                {
                    throw new SQLException("The configured non-JTA DataSource is invalid.");
                }
                return (DataSource) value;
            }
            finally
            {
                context.close();
            }
        }
        catch (NamingException exception)
        {
            throw new SQLException("Unable to look up the configured non-JTA DataSource.", exception);
        }
    }
}
