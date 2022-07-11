grammar Jova;

// lexer rules
KEY_CLASS : 'class';
KEY_IF : 'if';
KEY_ELSE : 'else';
KEY_WHILE : 'while';
KEY_RETURN : 'return';
KEY_NEW : 'new';
KEY_NIX : 'nix';
KEY_THIS : 'this';

AMOD : 'public' | 'private';

PRIMITIVE_TYPE : 'int' | 'String' | 'bool';

CLASS_TYPE : UPPERCASE (LETTER | DIGIT0 | '_')*;

ASSIGN : '=';
RELOP : '<' | '>' | '<=' | '>=' | '==' | '!=';
MULOP : '*' | '/' | '%';
SHFOP : '<<' | '>>';
AND : '&&';
OR : '||';
ADDOP : '+' | '-';
NOT : '!';
DOTOP : '.';

INT_LIT : '0' | DIGIT DIGIT0*;
BOOL_LIT : 'true' | 'false';

fragment DIGIT : '1'..'9';
fragment DIGIT0 : '0'..'9';

ID : LOWERCASE (LETTER | DIGIT0 | '_')*;

fragment LETTER : LOWERCASE | UPPERCASE;

fragment UPPERCASE : 'A'..'Z';

fragment LOWERCASE : 'a'..'z';

STRING_LIT : '"' (~[\n\r\\"] | ESQ_SEQ)* '"';

fragment ESQ_SEQ : '\\' [nrtbf"'\\];

COMMENT : '//' ~[\n\r]* -> skip;

BLOCK_COMMENT : '/*' .*? '*/' -> skip;

WS : [ \n\t\r] -> skip;

// parser rules
program : clazz+ EOF;

type : PRIMITIVE_TYPE | CLASS_TYPE;

clazz : classHead classBody;

classHead : KEY_CLASS CLASS_TYPE;

classBody : '{' member*  (method | constructor)* '}';

constructor : CLASS_TYPE parameters constructorBody ;

constructorBody : '{' variable* statement* '}';

member : AMOD type idList ';';

idList : ids+=ID (',' ids+=ID)* ;

method : methodHead methodBody;

methodHead : AMOD type ID parameters;

parameters : '(' parameterList? ')';

parameterList : types+=type ids+=ID (',' types+=type ids+=ID)*;

methodBody : '{' variable* statement* returnStatement '}';

statement
    : basicStatement
    | controlStatement
    ;

basicStatement
    : assignment ';'
    | expression ';'
    ;

compound : '{' statement* '}';

variable : type idList ';';

returnStatement : KEY_RETURN expression ';';

assignment : assignableExpression ASSIGN expression;

assignableExpression : (ID | KEY_THIS) chainedId*;

chainedId : DOTOP ID;

methodInvocation : ID '(' argumentList? ')';

chainedMethodInvocation : DOTOP methodInvocation;

idExpression : assignableExpression chainedMethodInvocation?;

argumentList : arguments+=expression (',' arguments+=expression)*;

expression
    : primaryExpression
    | objectAllocation
    | lhs=expression operator=MULOP rhs=expression
    | lhs=expression operator=SHFOP rhs=expression
    | lhs=expression operator=ADDOP rhs=expression
    | lhs=expression operator=RELOP rhs=expression
    | lhs=expression operator=AND   rhs=expression
    | lhs=expression operator=OR    rhs=expression
    | <assoc=right> condition=expression conditionOperator='?' lhs=expression operator=':' rhs=expression
    ;

unaryExpression : (op=NOT | op=ADDOP) primaryExpression;

primaryExpression
    : literal
    | idExpression
    | methodInvocation
    | parenthesisExpression
    | unaryExpression
    ;

objectAllocation : KEY_NEW CLASS_TYPE constructorArguments?;

constructorArguments : '(' argumentList? ')';

parenthesisExpression : '(' expression ')';

literal
    : INT_LIT
    | BOOL_LIT
    | STRING_LIT
    | KEY_NIX
    ;

controlStatement
    : ifStatement
    | whileStatement
    ;

ifStatement : KEY_IF '(' condition=expression ')' thenBranch=compound (KEY_ELSE elseBranch=compound)?;

whileStatement : KEY_WHILE '(' basicStatement* condition=expression ')' compound;
