package ru.comgrid.bot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.pgvector.PGvector;

/**
 * Simple JDBC repository for storing notes with pgvector embeddings.
 */
public class NoteRepository {
    private final Connection conn;

    public NoteRepository(Connection conn) throws SQLException {
        this.conn = conn;
        PGvector.registerTypes(conn);
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE EXTENSION IF NOT EXISTS vector");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS notes (id bigserial PRIMARY KEY, note_text text, embedding vector(1536))");
        }
    }

    public void saveNote(String text, double[] embedding) throws SQLException {
        String sql = "INSERT INTO notes (note_text, embedding) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, text);
            ps.setObject(2, new PGvector(toFloatArray(embedding)));
            ps.executeUpdate();
        }
    }

    public List<String> searchSimilar(double[] vector, int limit) throws SQLException {
        String sql = "SELECT note_text FROM notes ORDER BY embedding <=> ? LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, new PGvector(toFloatArray(vector)));
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        }
    }

    private static float[] toFloatArray(double[] arr) {
        float[] out = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = (float) arr[i];
        }
        return out;
    }
}

