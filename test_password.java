import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "password123";
        String hash = "$2a$10$xvPZ.xJH8QvwPOvHPqLQr.EYJZr7CdJOcW3d7.VPEw7GJZP0kXqHS";
        
        System.out.println("Testing password: " + password);
        System.out.println("Against hash: " + hash);
        System.out.println("Match: " + encoder.matches(password, hash));
        
        // Générer un nouveau hash
        String newHash = encoder.encode(password);
        System.out.println("\nNouveau hash généré: " + newHash);
        System.out.println("Nouveau hash match: " + encoder.matches(password, newHash));
    }
}
