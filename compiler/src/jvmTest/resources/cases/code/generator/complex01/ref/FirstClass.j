.source noSource
.class public FirstClass
.super java/lang/Object
.field private fieldPrivateInt I
.field public fieldPublicInt I
.field public fieldPublicBool Z
.field public fieldPublicString Ljava/lang/String;
.method public <init>()V
  aload_0
  invokespecial java/lang/Object/<init>()V
  return
.end method

.method public methodPublicInt(ZLFirstClass;)I
.limit stack 10
.limit locals 3
  ldc 0
  ireturn
.end method

.method public methodPublicBool(ZLFirstClass;)Z
.limit stack 10
.limit locals 3
  ldc 1
  ireturn
.end method

.method public conditionals()I
.limit stack 10
.limit locals 3
  iload 1
  iload 2
  if_icmple L_0
  ldc 0
  goto L_1
  L_0:
  ldc 1
  L_1:
  ifeq L_2
  ldc "then"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  goto L_3
  L_2:
  ldc "else"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  L_3:
  ldc 1
  ifeq L_4
  ldc "then"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  goto L_5
  L_4:
  L_5:
  L_6:
  ldc 0
  ifeq L_7
  ldc "while"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 1
  goto L_6
  L_7:
  iload 1
  ireturn
.end method

.method public conditionals2()I
.limit stack 10
.limit locals 4
  new java/util/Scanner
  dup
  getstatic java/lang/System/in Ljava/io/InputStream;
  invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
  astore_1
  ldc 1
  ifeq L_8
  ldc "then"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  goto L_9
  L_8:
  ldc "else"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  L_9:
  L_10:
  ldc 0
  ifeq L_11
  ldc "while"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  goto L_10
  L_11:
  aload_1
  invokevirtual java/util/Scanner/nextLine()Ljava/lang/String;
  astore 3
  iload 2
  ireturn
.end method

