grammar FubanScript ;

body : statement* ;

instruction : ID '(' arguments ')' ;

arguments : (expression (',' expression)*)? ;

functionArguments
    : (type ID (',' type ID)*)?
    ;

codebody: '{' body '}';

functionDeclaration : 'fun' ID '(' functionArguments ')' codebody ;

statement
   : functionDeclaration
   | instruction
   | ifStatement
   | whileStatement
   | returnStatement
   | setVariable
   | redefineVariable
   ;

ifStatement : 'if' OPAR expression CPAR codebody ('else' (ifStatement | codebody))? ;
whileStatement : 'while' OPAR expression CPAR codebody ;
returnStatement : 'return' expression? ;

setVariable : type ID '=' expression ;

redefineVariable : ID '=' expression ;

expression
   : expression op=(PLUS | MINUS | MULTIPLICATION | DIVISION | MOD | EXPONENTIAL) expression # additiveExpr
   | expression op=(LTEQ | GTEQ | LT | GT) expression # relationalExpr
   | expression op=(EQ | NEQ) expression # equalityExpr
   | expression '?:' expression # elvisExpr
   | atom # atomExpr
   | instruction # instructionExpr
   ;

atom
   : INT # integerAtom
   | DOUBLE # doubleAtom
   | (TRUE | FALSE) # booleanAtom
   | ID # idAtom
   | STRING # stringAtom
   | NULL # nullAtom
   ;

// Needs to be here because if it is after the ID declaration... it breaks for some reason
STR_TYPE: 'String' ;
INT_TYPE: 'Int' ;
DOUBLE_TYPE: 'Double' ;
BOOLEAN_TYPE: 'Boolean' ;
ANY_TYPE: 'Any' ;

type
   : STR_TYPE MAYBE_NULL?
   | INT_TYPE MAYBE_NULL?
   | DOUBLE_TYPE MAYBE_NULL?
   | BOOLEAN_TYPE MAYBE_NULL?
   | ANY_TYPE MAYBE_NULL?
   ;

INT: [0-9]+ ;
DOUBLE: [0-9.]+ ;
STRING: '"' (~["\r\n] | '""')* '"' ;
ID : [a-zA-Z0-9]+;
WS: [ \t\n\r]+ -> skip ;

// Tokens and stuff
OPAR : '(';
CPAR : ')';

EQ : '==';
NEQ : '!=';

GT : '>';
LT : '<';
GTEQ : '>=';
LTEQ : '<=';

PLUS : '+';
MINUS: '-';

MULTIPLICATION : '*';
DIVISION: '/';
MOD: '%';
EXPONENTIAL: '^';

TRUE: 'true';
FALSE: 'false';

NULL : 'null';

MAYBE_NULL: '?';

LINE_COMMENT
    : '//' ~[\r\n]* -> skip ;