Danny Yu, dyu6@binghamton.edu
Java

Encryption Code:

public static byte[] encrypt(String algo, Key key, byte[] data) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(algo);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    return cipher.doFinal(data);
}

Decryption Code:

public static byte[] decrypt(String algo, Key key, byte[] data) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(algo);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(data);
}

Encryption code for 'passwd' can be found in PasswordHasher.java, encryption code for keys can be found in Keygen.java, and use of the functions above can be found in both ATM.java and BankServer.java.

Tested on Bingsuns?
Yes - Works, but you MUST generate a new passwd file because of String encoding differences. Please see below.

How to execute:
1. make
2. passwd, balance, and keys should already be included. If not, run: 'java PasswordHasher', 'java BalanceGenerator', and 'java Keygen'.
3. passwd MUST be regenerated to work properly on bingsuns. Run 'java PasswordHasher'
3. Run BankServer: 'java BankServer 1337'
4. Run ATM: 'java ATM 127.0.0.1 1337'
5. Start typing into ATM.

Extra Credit Work:
None.

Special Notes:
Tested on 127.0.0.1, port 1337.
For "3. Quit" on ATM menu, the OS does not release socket, so it errors.
Otherwise it would continue listening for more ATM connections on the same port.

You MUST regenerate the passwd file for bingsuns!
Works fine on my own laptop, please let me know if bingsuns presents any more issues. Thanks!
