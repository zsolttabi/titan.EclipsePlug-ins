lexer grammar Ttcn3BaseLexer;

/*
 ******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************
*/

/*
 * author Arpad Lovassy
 */

@header {
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;

import org.eclipse.titan.designer.core.LoadBalancingUtilities;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.parsers.ParserMarkerSupport;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.common.parsers.IntervalDetector;
import org.eclipse.titan.common.parsers.Interval.interval_type;
import org.eclipse.titan.common.parsers.TITANMarker;
import org.eclipse.titan.designer.AST.Location;
}

@members {
  protected boolean isTTCNPP = false;
  private int tokenCounter = 0;

  public void setTTCNPP() {
  	isTTCNPP = true;
  }

  private IFile actualFile = null;
  
  public void setActualFile(IFile file) {
    actualFile = file;
  }
  
  private int actualLine = 1;
  
  public void setActualLine(int line) {
    actualLine = line;
  }

  /**
   * Creates and places a task marker on the provided location.
   * 
   * @param taskMarker the data of the marker needed for this operation
   * */
  public void createTaskMarker(TITANMarker taskMarker){
    if (actualFile == null) {
      return;
    }

    ParserMarkerSupport.createTaskMarker(actualFile, taskMarker);
  }
  
  /**
   * Creates and places a task marker on the provided location.
   * 
   * @param taskMarker the data of the marker needed for this operation
   */
  public void createWarningMarker(TITANMarker warningMarker) {
    if (actualFile == null) {
      return;
    }

    ParserMarkerSupport.createWarningMarker(actualFile, warningMarker);
  }

    IntervalDetector intervalDetector = new IntervalDetector();

  public Interval getRootInterval() {
    return intervalDetector.getRootInterval();
  }

  public void initRootInterval(int length) {
    intervalDetector.initRootInterval(length);
  }

  private Location lastComment = null;

  public Location getLastCommentLocation() {
  	return lastComment;
  }

  public void clearLastCommentLocation() {
  	lastComment = null;
  }
  
  private Location lastPPDirectiveLocation = null;
  
  public Location getLastPPDirectiveLocation() {
  	return lastPPDirectiveLocation;
  }

  //TODO: we will need it later for the performance
  /** Used to preload the class, also loading the TTCN-3 lexer. */
  public static void preLoad() {
  }
  
  protected void detectTasks(final String param, final boolean multiLine) {
		int todoIndex;
		int fixmeIndex;
		int newLineIndex;
		int lastIndex = 0;
		todoIndex = param.indexOf("TODO");
		fixmeIndex = param.indexOf("FIXME");
		int linecount = 0;

		while (todoIndex != -1 || fixmeIndex != -1) {
			int start;
			if (todoIndex > 0) {
				if (fixmeIndex > 0 && fixmeIndex < todoIndex) {
					start = fixmeIndex;
				} else {
					start = todoIndex;
				}
			} else {
				start = fixmeIndex;
			}
			newLineIndex = param.indexOf('\n', lastIndex);
			if (newLineIndex == -1 || newLineIndex > start) {
				if (newLineIndex == -1) {
					if(multiLine) {
					  newLineIndex = param.length() - 2;
					} else {
					  newLineIndex = param.length();
					}
				}

				String text = param.substring(start, newLineIndex);
				if (start == todoIndex) {
					createTaskMarker(new TITANMarker(text, actualLine + linecount + _tokenStartLine, -1, -1,
							IMarker.SEVERITY_INFO, IMarker.PRIORITY_NORMAL));
				} else {
					createTaskMarker(new TITANMarker(text, actualLine + linecount + _tokenStartLine, -1, -1,
							IMarker.SEVERITY_INFO, IMarker.PRIORITY_HIGH));
				}
			}

			linecount++;
			lastIndex = newLineIndex + 1;
			todoIndex = param.indexOf("TODO", lastIndex);
			fixmeIndex = param.indexOf("FIXME", lastIndex);
		}
  }
}

/*------------------------------------------- Keywords -------------------------------------------*/

tokens {
  ACTION,                 ACTIVATE,       ADDRESS,
  ALIVE,                   ALL,                 ALT,
  ALTSTEP,               AND,                 AND4B,
  ANY,                       ANYTYPE,         APPLY,
  
  BITSTRING,           BOOLEAN,         BREAK,

  CALL,                     CASE,               CATCH,
  CHARKEYWORD,              CHARSTRING,   CHECK,
  CLEAR,                   COMPLEMENTKEYWORD,  COMPONENT,
  CONNECT,               CONST,             CONTINUE,
  CONTROL,               CREATE,

  DEACTIVATE,         DEFAULT,         DEREFERS,
  DISCONNECT,         DISPLAY,         DECVALUE,
  DO,                         DONE,

  ELSE,                     ENCODE,           ENUMERATED,
  ERROR,                   EXCEPT,           EXCEPTION,
  EXECUTE,               EXTENDS,         ENCVALUE,
  EXTENSION,           EXTERNAL,

  FAIL,                     FALSE,             FLOAT,
  FOR,                       FRIEND,           FROM,
  FUNCTION,

  GETCALL,               GETREPLY,       GETVERDICT,
  GOTO,                     GROUP,

  HALT,                     HEXSTRING,

  IF,                         IFPRESENT,     IMPORT,
  IN,                         INCONC,           INFINITY,
  INOUT,                   INTEGER,         INTERLEAVE,

  KILL,                     KILLED,

  LABEL,                   LANGUAGE,       LENGTH,
  LOG,

  MAP,                       MATCH,             MESSAGE,
  MIXED,                   MOD,                 MODIFIES,
  MODULE,                 MODULEPAR,     MTC,

  NOBLOCK,               NONE,
  NOT,                       NOT4B,             NOWAIT,
  NOT_A_NUMBER,     NULL1,              NULL2,

  OBJECTIDENTIFIERKEYWORD, OCTETSTRING, OF,
  OMIT,                     ON,                   OPTIONAL,
  OR,                         OR4B,               OUT,
  OVERRIDEKEYWORD,

  PARAM,                   PASS,               PATTERNKEYWORD,
  PERMUTATION,       PORT,               PUBLIC,
  PRESENT,				PRIVATE,         PROCEDURE,

  RAISE,                   READ,               RECEIVE,
  RECORD,                 RECURSIVE,     REFERS,
  REM,                       REPEAT,           REPLY,
  RETURN,                 RUNNING,         RUNS,

  SELECT,                 SELF,               SEND,
  SENDER,                 SET,                 SETVERDICT,
  SIGNATURE,           START,
  STOP,                     SUBSET,           SUPERSET,
  SYSTEM,

  TEMPLATE,             TESTCASE,       TIMEOUT,
  TIMER,                   TO,                   TRIGGER,
  TRUE,                     TYPE,

  UNION,                   UNIVERSAL,     UNMAP,

  VALUE,                   VALUEOF,         VAR,
  VARIANT,               VERDICTTYPE,

  WHILE,                   WITH,

  XOR,                       XOR4B,


  /*------------------------------ Predefined function identifiers --------------------------------*/
  
  BIT2HEX,	BIT2INT,		BIT2OCT,	BIT2STR,
  
  CHAR2INT,	CHAR2OCT,
  
  DECOMP,
  
  ENUM2INT,
 
  FLOAT2INT,	FLOAT2STR,
  
  HEX2BIT,	HEX2INT,	HEX2OCT,	HEX2STR,
  
  INT2BIT,	INT2CHAR,	INT2ENUM,	INT2FLOAT,	INT2HEX,
  INT2OCT,	INT2STR,	INT2UNICHAR,
  
  ISVALUE,	ISCHOSEN,	ISPRESENT,  ISBOUND,
  
  LENGTHOF,  LOG2STR,
  
  OCT2BIT,	OCT2CHAR,	OCT2HEX,
  OCT2INT,	OCT2STR,
  
  REGEXP,	REPLACE,	RND,
  
  SIZEOF,
  
  STR2BIT,	STR2FLOAT,	STR2HEX,
  STR2INT,	STR2OCT,
  
  SUBSTR,
  
  TESTCASENAME,
  
  UNICHAR2INT,	UNICHAR2CHAR,
  
  TTCN2STRING, STRING2TTCN,
  
  GET_STRINGENCODING, OCT2UNICHAR, REMOVE_BOM,
  UNICHAR2OCT, ENCODE_BASE64, DECODE_BASE64,
  
  /* general macro, used for code completion, see TTCN3KeywordLessLexer4 */
  MACRO
  
}

WS:				[ \t\r\n\f]+	-> skip;

LINE_COMMENT:	'//' ~[\r\n]*	{detectTasks(getText(), false);} -> skip ;

BLOCK_COMMENT:	'/*' .*? '*/'	{
	intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.MULTILINE_COMMENT);
	intervalDetector.popInterval(_input.index(), _interp.getLine());
} {detectTasks(getText(), true);} -> skip ;

//TODO: check that nothing else preceeds it in current line
PREPROCESSOR_DIRECTIVE:
//TODO
//{ int ppLine = getLine(); int ppOffset = getOffset(); }
(   '#'
	(
		{ isTTCNPP }? (
			~('\n'|'\r'|'\\')
			|	{ _input.LA(2)!='\n' && _input.LA(2)!='\r' }? '\\'
			|	( '\\\n' | '\\\r' | '\\\r\n' )
		)*
	|
		{ !isTTCNPP }? ( (' '|'\t')* ('0'..'9')+ (' '|'\t')+ CSTRING ('0'..'9'|' '|'\t')* )
	)
)
//TODO
//
//{
//	lastPPDirectiveLocation = new Location(actualFile, ppLine, ppOffset, getOffset());
//}
{if (!isTTCNPP) {skip();};};

IDENTIFIER:
	[A-Za-z][A-Za-z0-9_]*
;

ASSIGNMENTCHAR:	':=';

PORTREDIRECTSYMBOL:	'->';

CSTRING:
'"'
(	'\\' .
|	'""'
|	~( '\\' | '"' )
)*
'"'
;

fragment BIN: [01];
fragment BINORMATCH: BIN | '?' | '*';
fragment HEX: [0-9A-Fa-f];
fragment HEXORMATCH: HEX | '?' | '*';
fragment OCT: HEX HEX;
fragment OCTORMATCH: OCT | '?' | '*';

// Corresponds to titan/compiler2/ttcn3/compiler.l
// TTCN-3 standard is more strict, it does NOT allow whitespaces between the bin/hex/oct digits
BSTRING:		'\'' WS? ( BIN        WS? )* '\'B';
BSTRINGMATCH:	'\'' WS? ( BINORMATCH WS? )* '\'B';
HSTRING:		'\'' WS? ( HEX        WS? )* '\'H';
HSTRINGMATCH:	'\'' WS? ( HEXORMATCH WS? )* '\'H';
OSTRING:		'\'' WS? ( OCT        WS? )* '\'O';
OSTRINGMATCH:	'\'' WS? ( OCTORMATCH WS? )* '\'O';

// Macros
MACRO_MODULEID:			'%moduleId' | '__MODULE__';
MACRO_DEFINITION_ID:	'%definitionId';
MACRO_TESTCASEID:		'%testcaseId' | '__TESTCASE__';
MACRO_FILENAME:			'%fileName';
MACRO_LINENUMBER:		'%lineNumber';
MACRO_FILEPATH:			'__FILE__';
MACRO_BFILENAME:		'__BFILE__';
MACRO_LINENUMBER_C:		'__LINE__';
MACRO_SCOPE:			'__SCOPE__';

// TITAN specific keywords
TITANSPECIFICTRY:	'@try';
TITANSPECIFICCATCH:	'@catch';
TITANSPECIFICLAZY:	'@lazy';

fragment DIGIT: [0-9];

fragment INT: [1-9] DIGIT* | '0';

NUMBER: INT;

// Corresponds to FLOAT in titan/compiler2/ttcn3/compiler.l
// TTCN-3 standard is more strict, it allows only 'E' '-'? in the exponent part
FLOATVALUE:
(	INT '.' DIGIT+
|	INT ( '.' DIGIT+ )? [Ee] [+-]? INT
);

RANGEOP: '..';

DOT: '.';

SEMICOLON: ';';

COMMA: ',';

COLON: ':';

BEGINCHAR:
	'{'
{
  intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.NORMAL);
};

ENDCHAR:
	'}'
{
  intervalDetector.popInterval(_tokenStartCharIndex, _tokenStartLine);
};

SQUAREOPEN:
	'['
{
  intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.INDEX);
};

SQUARECLOSE:
	']'
{
  intervalDetector.popInterval(_tokenStartCharIndex, _tokenStartLine);
};

LPAREN:
	'('
{
  intervalDetector.pushInterval(_tokenStartCharIndex, _tokenStartLine, interval_type.PARAMETER);
};

RPAREN:
	')'
{
  intervalDetector.popInterval(_tokenStartCharIndex, _tokenStartLine);
};

LESSTHAN: '<';

MORETHAN: '>';

NOTEQUALS: '!=';

MOREOREQUAL: '>=';

LESSOREQUAL: '<=';

EQUAL: '==';

PLUS: '+';

MINUS: '-';

STAR: '*';

SLASH: '/';

EXCLAMATIONMARK: '!';

QUESTIONMARK: '?';

SHIFTLEFT: '<<';

SHIFTRIGHT: '>>';

ROTATELEFT: '<@';

ROTATERIGHT: '@>';

STRINGOP: '&';

//TODO: remove if not needed
LEXERPLACEHOLDER: 'meaningless text just to have a last token' ;