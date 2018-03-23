import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
class RSA {
    private BigInteger p; // First prime
    private BigInteger q; // Second prime
    private BigInteger n; // Mod
    private BigInteger t; // Euler totient (Phi)
    private BigInteger pubKey;
    private BigInteger prvKey;
    private Random rnd;

    public RSA(int bitLength){
        this.rnd = new SecureRandom();
        this.p = new BigInteger(bitLength/2,
                100,rnd);
        this.q = new BigInteger(bitLength/2,
                100,rnd);
        this.n = q.multiply(p);
        this.t = p.subtract(BigInteger.ONE).multiply
                (q.subtract(BigInteger.ONE));
    }

    public void generateKeys(){
        pubKey = new BigInteger(t.bitLength(), rnd);
        while(pubKey.compareTo(BigInteger.ONE) <= 0
                || pubKey.compareTo(t) >= 0
                || !pubKey.gcd(t).equals(BigInteger.ONE)){
            pubKey = new BigInteger(t.bitLength(), rnd);
        }
        prvKey = pubKey.modInverse(t);
    }

    public static BigInteger encrypt(String plaintext, BigInteger pubKey, BigInteger mod){
        byte[] bytes = plaintext.getBytes();
        BigInteger data = new BigInteger(bytes);
        return data.modPow(pubKey, mod);
    }

    public BigInteger encrypt(String plaintext){
        return encrypt(plaintext, this.pubKey, this.n);
    }

    public static String decrypt(BigInteger ciphertext, BigInteger prvKey, BigInteger mod){
        ciphertext = ciphertext.modPow(prvKey, mod);
        byte[] bytes = ciphertext.toByteArray();
        String plaintext = new String(bytes);
        return plaintext;
    }

    public String decrypt(BigInteger ciphertext){
        return decrypt(ciphertext, this.prvKey, this.n);
    }

    public BigInteger getPrv(){
        return this.prvKey;
    }

    public BigInteger getPub(){
        return this.pubKey;
    }

    public BigInteger getMod(){
        return this.n;
    }

    public RSA(){
        this(2048);
    }
//}

    public static void main(String[] args) {
        RSA rsa = new RSA();
        RSA rsa2 = new RSA();
        rsa.generateKeys();
        rsa2.generateKeys();
        String in = "Hello world!";
        BigInteger enc = rsa.encrypt(in);
        System.out.println("Encrypted: " + enc.toString());
        System.out.println("Decrypted: " + rsa.decrypt(enc));
        BigInteger enc2 = encrypt(rsa.decrypt(enc), rsa2.getPub(), rsa2.getMod());
        System.out.println("Encrypted: " + enc2.toString());
        System.out.println("Decrypted: " + decrypt(enc2, rsa2.getPrv(), rsa2.getMod()));
        String s = "[A]: Hello";
        System.out.println(s.substring(s.indexOf('[') + 1,(s.indexOf(']'))));
        System.out.println(s.substring((s.indexOf(' ') + 1)));
        //if list.contains(item)
    }
}