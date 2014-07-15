JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ATM.java \
	BankServer.java \
	PasswordHasher.java \
	KeyGen.java \
	BalanceGenerator.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class *.key balance passwd *~ *.*~