package ru.comgrid.bot;

import com.pgvector.PGvector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDao {
    private final String url;
    private final String user;
    private final String password;

    public NoteDao(String url, String user, String password) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        init();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void init() throws SQLException {
        try (Connection conn = connect()) {
            PGvector.addVectorType(conn);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notes (id bigserial PRIMARY KEY, note_text text, embedding vector(1536))");
            }
        }
    }

    public void insert(String text, float[] embedding) throws SQLException {
        try (Connection conn = connect()) {
            PGvector.addVectorType(conn);
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO notes (note_text, embedding) VALUES (?, ?)");) {
                ps.setString(1, text);
                ps.setObject(2, new PGvector(embedding));
                ps.executeUpdate();
            }
        }
    }

    public List<String> search(float[] embedding, int limit) throws SQLException {
        List<String> results = new ArrayList<>();
        try (Connection conn = connect()) {
            PGvector.addVectorType(conn);
            try (PreparedStatement ps = conn.prepareStatement("SELECT note_text FROM notes ORDER BY embedding <=> ? LIMIT ?")) {
                ps.setObject(1, new PGvector(embedding));
                ps.setInt(2, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(rs.getString("note_text"));
                    }
                }
            }
        }
        return results;
    }
}
