.source noSource
.class public Main
.super java/lang/Object
.method public static main([Ljava/lang/String;)V
.limit stack 10
.limit locals 3
  ldc "Set it to W for WUMBO!\n"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  ldc 1
  istore 2
  iload 2
  ifeq L_0
  ldc "MAGNIFY!\n"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  goto L_1
  L_0:
  ldc "EEEVIL!\n"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  L_1:
  ldc 0
  invokestatic java/lang/System/exit(I)V
  return
.end method

