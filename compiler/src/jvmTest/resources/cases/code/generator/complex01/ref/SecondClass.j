.source noSource
.class public SecondClass
.super java/lang/Object
.field private fieldPrivateFirstClass LFirstClass;
.field public fieldPublicFirstClass LFirstClass;
.method public <init>()V
.limit stack 10
.limit locals 1
  aload_0
  invokespecial java/lang/Object/<init>()V
  new FirstClass
  dup
  invokespecial FirstClass/<init>()V
  aload_0
  swap
  putfield SecondClass/fieldPrivateFirstClass LFirstClass;
  return
.end method

.method private methodPrivateSecondClass(LFirstClass;Z)LSecondClass;
.limit stack 10
.limit locals 4
  new SecondClass
  dup
  invokespecial SecondClass/<init>()V
  astore 3
  aload 3
  areturn
.end method

.method public conditionals()I
.limit stack 10
.limit locals 3
  new java/util/Scanner
  dup
  getstatic java/lang/System/in Ljava/io/InputStream;
  invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V
  astore_1
  ldc 1
  ifeq L_0
  ldc "true"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  goto L_1
  L_0:
  ldc "true"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  L_1:
  L_2:
  ldc 0
  ifeq L_3
  ldc "false"
  getstatic java/lang/System/out Ljava/io/PrintStream;
  swap
  invokevirtual java/io/PrintStream/print(Ljava/lang/String;)V
  ldc 0
  istore 2
  goto L_2
  L_3:
  aload_1
  invokevirtual java/util/Scanner/nextInt()I
  istore 2
  iload 2
  ireturn
.end method

