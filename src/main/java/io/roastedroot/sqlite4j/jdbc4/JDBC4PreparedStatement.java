package io.roastedroot.sqlite4j.jdbc4;

import io.roastedroot.sqlite4j.SQLiteConnection;
import io.roastedroot.sqlite4j.jdbc3.JDBC3PreparedStatement;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Arrays;

public class JDBC4PreparedStatement extends JDBC3PreparedStatement
        implements PreparedStatement, ParameterMetaData {

    @Override
    public String toString() {
        return sql + " \n parameters=" + Arrays.toString(batch);
    }

    public JDBC4PreparedStatement(SQLiteConnection conn, String sql) throws SQLException {
        super(conn, sql);
    }

    private static int requireLengthIsPositiveInt(long length)
            throws SQLFeatureNotSupportedException {
        if (length > Integer.MAX_VALUE || length < 0) {
            throw new SQLFeatureNotSupportedException(
                    "Data must have a length between 0 and Integer.MAX_VALUE");
        }
        return (int) length;
    }

    private byte[] readBytes(InputStream istream) throws SQLException {
        try {
            return istream.readAllBytes();
        } catch (IOException cause) {
            SQLException exception = new SQLException("Error reading stream");
            exception.initCause(cause);
            throw exception;
        }
    }

    // JDBC 4
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        // TODO Support this
        throw new SQLFeatureNotSupportedException();
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        setString(parameterIndex, value);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length)
            throws SQLException {
        setCharacterStream(parameterIndex, value, length);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        if (value == null) {
            setString(parameterIndex, null);
        } else {
            setCharacterStream(parameterIndex, value.getCharacterStream());
        }
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setCharacterStream(parameterIndex, reader, requireLengthIsPositiveInt(length));
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length)
            throws SQLException {
        setBinaryStream(parameterIndex, inputStream, requireLengthIsPositiveInt(length));
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        setClob(parameterIndex, reader, length);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        // TODO Support this
        throw new SQLFeatureNotSupportedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setAsciiStream(parameterIndex, x, requireLengthIsPositiveInt(length));
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length)
            throws SQLException {
        setBinaryStream(parameterIndex, x, requireLengthIsPositiveInt(length));
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length)
            throws SQLException {
        setCharacterStream(parameterIndex, reader, requireLengthIsPositiveInt(length));
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        byte[] bytes = readBytes(x);
        setAsciiStream(parameterIndex, new ByteArrayInputStream(bytes), bytes.length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        setBytes(parameterIndex, readBytes(x));
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader, Integer.MAX_VALUE);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setCharacterStream(parameterIndex, value);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        setCharacterStream(parameterIndex, reader, Integer.MAX_VALUE);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        setBytes(parameterIndex, readBytes(inputStream));
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        setClob(parameterIndex, reader);
    }
}
