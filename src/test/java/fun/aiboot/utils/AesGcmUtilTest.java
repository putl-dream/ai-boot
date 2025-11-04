package fun.aiboot.utils;

class AesGcmUtilTest {
    public static void main(String[] args) {
        String key = "你好";

        String ks = AesGcmUtil.generateKey();
        System.out.println(ks);

        String encrypt = AesGcmUtil.encrypt(key, ks);
        System.out.println(encrypt);

        String decrypt = AesGcmUtil.decrypt(encrypt, ks);
        System.out.println(decrypt);

    }
}