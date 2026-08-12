import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
        
        // Test verification
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("Verification: " + (matches ? "✓ Match" : "✗ No match"));
        
        // Test with the hash from database
        String dbHash = "$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS";
        boolean dbMatches = encoder.matches(rawPassword, dbHash);
        System.out.println("DB Hash Test: " + (dbMatches ? "✓ Match" : "✗ No match"));
    }
}
