/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.impex;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import sap.commerce.toolset.impex.psi.ImpExTypes;
import com.intellij.psi.TokenType;
import java.util.Collection;
import java.util.HashSet;

%%

%class ImpExLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%{
  private Collection<String> macroDeclarations = new HashSet<String>();

  // Queue for deferred tokens when a macro candidate is split
  private final java.util.Deque<IElementType> pendingTokenTypes   = new java.util.ArrayDeque<>();
  private final java.util.Deque<Integer>      pendingTokenLengths = new java.util.ArrayDeque<>();

  /**
   * Resolves a matched macro_usage candidate against declared macros.
   *
   * Exact match      → returns MACRO_USAGE, switches to returnState
   * Prefix match     → returns MACRO_USAGE for the prefix,
   *                    queues MACRO_VALUE for the suffix, switches to returnState
   * No match at all  → returns MACRO_VALUE for the full text, switches to returnState
   *
   * @param returnState  the yybegin() state to restore after resolution
   */
  private IElementType resolveMacroUsage(int returnState) {
      var candidate = yytext().toString();

      // 1. Exact match
      if (macroDeclarations.contains(candidate) || candidate.startsWith("$config-")) {
          yybegin(returnState);
          return ImpExTypes.MACRO_USAGE;
      }

      // 2. Longest declared prefix
      String bestMatch = null;
      for (String declared : macroDeclarations) {
          if (candidate.startsWith(declared)) {
              if (bestMatch == null || declared.length() > bestMatch.length()) {
                  bestMatch = declared;
              }
          }
      }

      if (bestMatch != null) {
          int suffixLen = candidate.length() - bestMatch.length();
          // Queue the suffix as MACRO_VALUE to be returned on next advance()
          pendingTokenTypes.addLast(ImpExTypes.MACRO_VALUE);
          pendingTokenLengths.addLast(suffixLen);
          yypushback(suffixLen);
          yybegin(returnState);
          return ImpExTypes.MACRO_USAGE;
      }

      // 3. No match — treat the whole token as a plain value
      yybegin(returnState);
      return ImpExTypes.MACRO_VALUE;
  }
%}

%eof{
    return;
%eof}
%ignorecase

// See SAP Commerce sources: ImpExConstants

identifier  = [a-zA-Z0-9_-]

crlf        = (([\n])|([\r])|(\r\n))
not_crlf    = [^\r\n]
white_space = [ \t\f]
backslash   = [\\]

multiline_separator   = {backslash}{crlf}

line_comment = "#"|"#"[^%\n]{not_crlf}*

single_string = ['](('')|([^'\r\n])*)[']
// Double string can contain line break
double_string = [\"](([\"][\"])|[^\"])*[\"]

// See supported context here -> ImpExReader.getScriptExecutionContext
script_marker_groovy = "#%groovy%"
script_marker_javascript = "#%javascript%"
script_marker_bsh = "#%"("bsh%")?

script_action_if = (if)[:]
script_action_endif = (endif)[:]
script_action_beforeEach = (beforeEach)[:]
script_action_afterEach = (afterEach)[:]
script_body_value = [^ '\"\r\n]+

macro_name_declaration = [$](([\w\d-]|{white_space})+({backslash}\s*)*)+[=]
root_macro_usage       = [$]([\.\(\)a-zA-Z0-9_-])+
macro_usage            = [$](config-)?({identifier}({dot})?)+
macro_config_usage     = [$](config-)({identifier}({dot})?)+
macro_value            = ({not_crlf}|({identifier}({dot})?)+|({backslash}\s*)+)

left_square_bracket  = [\[]
right_square_bracket = [\]]

left_round_bracket  = [\(]
right_round_bracket = [\)]

semicolon     = [;]
comma         = [,]
dot           = [.]
assign_value  = [=]

double_quote   = [\"]
double_quote_escaped = [\"][\"]
single_quote   = [']
string_literal = ({not_crlf}|{identifier}+)

// see - CollectionValueTranslator
// value must start with this prefix
collection_append_prefix = "(+)"
collection_remove_prefix = "(-)"
collection_merge_prefix  = "(+?)"

default_path_delimiter      = [:]
default_key_value_delimiter = "->"
alternative_map_delimiter   = [|]

// see - AtomicValueTranslator
boolean = ("true"|"false"|"ja"|"nein"|"wahr"|"falsch"|"y"|"n"|-|\+)

digit   = [-+]?[0-9]+([.][0-9]+)?
//class_with_package = ({identifier}+[.]{identifier}+)+

parameter_name = {identifier}+
alternative_pattern = [|]

special_parameter_marker = [@]
special_parameter_value = [^(\;\[\"\r\n\\\ \t\f$]+
special_parameter_end = [(\;\[\"\r\n\\\$]

attribute_name  = ({identifier}|[.])+
attribute_value = [^, \t\f\]\r\n]+

document_id = [&]{identifier}+

header_mode_insert        = "INSERT"
header_mode_update        = "UPDATE"
header_mode_insert_update = "INSERT_UPDATE"
header_mode_remove        = "REMOVE"

header_type = {identifier}+

value_subtype      = {identifier}+
field_value        = ({not_crlf}|{identifier}+)
//field_value_url    = ([/]{identifier}+)+[.]{identifier}+
field_value_ignore = "<ignore>"
field_value_null   = "<null>"
field_value_prefix_password_encoding = "*:" | "plain:" | "sha-256:" | "sha-512:" | "md5:" | "pbkdf2:"

tag_open    = [<][^/]({identifier})+{white_space}*[>]
tag_close    = [<][/]({identifier})+{white_space}*[>]

start_userrights                  = [$]START_USERRIGHTS
end_userrights                    = [$]END_USERRIGHTS

%state YYINITIAL_DOUBLE_STRING
%state WAITING_MACRO_VALUE
%state MACRO_DECLARATION
%state HEADER_TYPE
%state HEADER_LINE
%state SPECIAL_PARAMETER
%state FIELD_VALUE
%state DOUBLE_STRING
%state FIELD_VALUE_START
%state BEAN_SHELL
%state SCRIPT
%state SCRIPT_BODY
%state SCRIPT_BODY_MULTILINE
%state SCRIPT_DOUBLE_STRING
%state MODIFIERS_BLOCK
%state WAITING_ATTR_OR_PARAM_VALUE
%state HEADER_PARAMETERS
%state MACRO_USAGE
%state MACRO_USAGE_CANDIDATE
%state MACRO_CONFIG_USAGE
%state WAITING_MACRO_CONFIG_USAGE
%state USER_RIGHTS_START
%state USER_RIGHTS_END
%state USER_RIGHTS_HEADER_LINE
%state USER_RIGHTS_WAIT_FOR_VALUE_LINE
%state USER_RIGHTS_VALUE_LINE

%%

{white_space}+                                              { return TokenType.WHITE_SPACE; }

<YYINITIAL> {
    {line_comment}                                          { return ImpExTypes.LINE_COMMENT; }

    {script_marker_groovy}                                  { yybegin(SCRIPT_BODY); return ImpExTypes.GROOVY_MARKER; }
    {script_marker_javascript}                              { yybegin(SCRIPT_BODY); return ImpExTypes.JAVASCRIPT_MARKER; }
    {script_marker_bsh}                                     { yybegin(SCRIPT_BODY); return ImpExTypes.BEAN_SHELL_MARKER; }

    {double_quote}                                          { yybegin(YYINITIAL_DOUBLE_STRING); return ImpExTypes.DOUBLE_QUOTE_OPEN; }

    {start_userrights}                                      { yybegin(USER_RIGHTS_START); return ImpExTypes.START_USERRIGHTS; }
    {root_macro_usage}                                      { return ImpExTypes.MACRO_USAGE; }
    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
    {macro_name_declaration}                                {
                                                              yybegin(MACRO_DECLARATION);
                                                              /* Push back '='. */
                                                              yypushback(1);
                                                              /* Push back spaces. */
                                                              var macroName = yytext().toString().trim();
                                                              yypushback(yylength() - macroName.length());
                                                              macroDeclarations.add(macroName);
                                                              return ImpExTypes.MACRO_NAME_DECLARATION;
                                                            }

    {header_mode_insert}                                    { yybegin(HEADER_TYPE); return ImpExTypes.HEADER_MODE_INSERT; }
    {header_mode_update}                                    { yybegin(HEADER_TYPE); return ImpExTypes.HEADER_MODE_UPDATE; }
    {header_mode_insert_update}                             { yybegin(HEADER_TYPE); return ImpExTypes.HEADER_MODE_INSERT_UPDATE; }
    {header_mode_remove}                                    { yybegin(HEADER_TYPE); return ImpExTypes.HEADER_MODE_REMOVE; }

    {value_subtype}                                         { yybegin(FIELD_VALUE); return ImpExTypes.VALUE_SUBTYPE; }
    {semicolon}                                             { yybegin(FIELD_VALUE_START); return ImpExTypes.FIELD_VALUE_SEPARATOR; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<YYINITIAL_DOUBLE_STRING> {
    {double_quote}                                          { yybegin(YYINITIAL); return ImpExTypes.DOUBLE_QUOTE_CLOSE; }
    {double_quote_escaped}                                  { return ImpExTypes.DOUBLE_QUOTE_ESCAPE; }
    {white_space}+                                          { return TokenType.WHITE_SPACE; }

    {script_marker_groovy}                                  { yybegin(SCRIPT_BODY_MULTILINE); return ImpExTypes.GROOVY_MARKER; }
    {script_marker_javascript}                              { yybegin(SCRIPT_BODY_MULTILINE); return ImpExTypes.JAVASCRIPT_MARKER; }
    {script_marker_bsh}                                     { yybegin(SCRIPT_BODY_MULTILINE); return ImpExTypes.BEAN_SHELL_MARKER; }

    {macro_usage}                                           {
                                                                var macroName = yytext().toString().trim();
                                                                return macroDeclarations.contains(macroName) || macroName.startsWith("$config-")
                                                                    ? ImpExTypes.MACRO_USAGE
                                                                    : ImpExTypes.STRING_LITERAL;
                                                            }
    {string_literal}                                        { return ImpExTypes.STRING_LITERAL; }

    {crlf}                                                  { return ImpExTypes.CRLF; }
}

<SCRIPT_BODY_MULTILINE> {
    {double_quote}                                          { yybegin(YYINITIAL); return ImpExTypes.DOUBLE_QUOTE_CLOSE; }
    {double_quote_escaped}                                  { return ImpExTypes.DOUBLE_QUOTE_ESCAPE; }
    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
    {single_string}                                         { return ImpExTypes.SINGLE_STRING; }
    {script_action_if}                                      { return ImpExTypes.SCRIPT_ACTION_IF; }
    {script_action_endif}                                   { return ImpExTypes.SCRIPT_ACTION_ENDIF; }
    {script_action_beforeEach}                              { return ImpExTypes.SCRIPT_ACTION_BEFOREEACH; }
    {script_action_afterEach}                               { return ImpExTypes.SCRIPT_ACTION_AFTEREACH; }
    {script_body_value}                                     { return ImpExTypes.SCRIPT_BODY_VALUE; }
    {crlf}                                                  { return ImpExTypes.CRLF; }
}

<USER_RIGHTS_START> {
    {semicolon}                                             { return ImpExTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(USER_RIGHTS_HEADER_LINE); return ImpExTypes.CRLF; }
}

<USER_RIGHTS_HEADER_LINE> {
    "Type"                                                  { return ImpExTypes.TYPE; }
    "UID"                                                   { return ImpExTypes.UID; }
    "MemberOfGroups"                                        { return ImpExTypes.MEMBEROFGROUPS; }
    "Password"                                              { return ImpExTypes.PASSWORD; }
    "Target"                                                { return ImpExTypes.TARGET; }
    {identifier}+                                           { return ImpExTypes.PERMISSION; }
    {line_comment}                                          { return ImpExTypes.LINE_COMMENT; }
    {semicolon}                                             { yybegin(USER_RIGHTS_WAIT_FOR_VALUE_LINE); return ImpExTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return ImpExTypes.END_USERRIGHTS; }
    {crlf}                                                  { return ImpExTypes.CRLF; }
}

<USER_RIGHTS_WAIT_FOR_VALUE_LINE> {
    "Type"                                                  { return ImpExTypes.TYPE; }
    "UID"                                                   { return ImpExTypes.UID; }
    "MemberOfGroups"                                        { return ImpExTypes.MEMBEROFGROUPS; }
    "Password"                                              { return ImpExTypes.PASSWORD; }
    "Target"                                                { return ImpExTypes.TARGET; }
    {identifier}+                                           { return ImpExTypes.PERMISSION; }
    {semicolon}                                             { return ImpExTypes.PARAMETERS_SEPARATOR; }

    {end_userrights}                                        { yybegin(YYINITIAL); return ImpExTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return ImpExTypes.CRLF; }
}

<USER_RIGHTS_VALUE_LINE> {
// even if we may have one more Header line in the body of the user rights, it will be ignored by ImportExportUserRightsHelper
//    {user_rights_type}                                      { yybegin(USER_RIGHTS_HEADER_LINE); yypushback(yylength()); }
    "-"                                                     { return ImpExTypes.PERMISSION_DENIED; }
    "+"                                                     { return ImpExTypes.PERMISSION_ALLOWED; }
    {identifier}+                                           { return ImpExTypes.FIELD_VALUE; }
    {line_comment}                                          { return ImpExTypes.LINE_COMMENT; }
    {semicolon}                                             { return ImpExTypes.FIELD_VALUE_SEPARATOR; }
    {dot}                                                   { return ImpExTypes.DOT; }
    {comma}                                                 { return ImpExTypes.COMMA; }

    {end_userrights}                                        { yybegin(USER_RIGHTS_END); return ImpExTypes.END_USERRIGHTS; }
    {crlf}                                                  { yybegin(USER_RIGHTS_VALUE_LINE); return ImpExTypes.CRLF; }
}

<USER_RIGHTS_END> {
    {semicolon}                                             { return ImpExTypes.PARAMETERS_SEPARATOR; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<SCRIPT_BODY> {
    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
    {script_action_if}                                      { return ImpExTypes.SCRIPT_ACTION_IF; }
    {script_action_endif}                                   { return ImpExTypes.SCRIPT_ACTION_ENDIF; }
    {script_action_beforeEach}                              { return ImpExTypes.SCRIPT_ACTION_BEFOREEACH; }
    {script_action_afterEach}                               { return ImpExTypes.SCRIPT_ACTION_AFTEREACH; }
    {single_string}                                         { return ImpExTypes.SINGLE_STRING; }

    {double_quote}                                          { yybegin(SCRIPT_DOUBLE_STRING); return ImpExTypes.DOUBLE_QUOTE_OPEN; }

    {script_body_value}                                     { return ImpExTypes.SCRIPT_BODY_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<SCRIPT_DOUBLE_STRING> {
    {double_quote}                                          { yybegin(SCRIPT_BODY); return ImpExTypes.DOUBLE_QUOTE_CLOSE; }
    {macro_usage}                                           {
                                                                var macroName = yytext().toString().trim();
                                                                return macroDeclarations.contains(macroName) || macroName.startsWith("$config-")
                                                                    ? ImpExTypes.MACRO_USAGE
                                                                    : ImpExTypes.STRING_LITERAL;
                                                            }
    {string_literal}                                        { return ImpExTypes.STRING_LITERAL; }
}

<FIELD_VALUE_START> {
    {double_quote}                                          { yybegin(DOUBLE_STRING); return ImpExTypes.DOUBLE_QUOTE_OPEN;}
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }

    /* anything else → fallback to normal FIELD_VALUE */
    .                                                       { yypushback(1); yybegin(FIELD_VALUE); }
}

<DOUBLE_STRING> {
    {double_quote}                                          { yybegin(FIELD_VALUE); return ImpExTypes.DOUBLE_QUOTE_CLOSE; }
    {double_quote_escaped}                                  { return ImpExTypes.DOUBLE_QUOTE_ESCAPE; }
    {white_space}+                                          { return TokenType.WHITE_SPACE; }

    {tag_open}                                              { return ImpExTypes.TAG_OPEN; }
    {tag_close}                                             { return ImpExTypes.TAG_CLOSE; }
    {boolean}                                               { return ImpExTypes.BOOLEAN; }
    {digit}                                                 { return ImpExTypes.DIGIT; }
    {comma}                                                 { return ImpExTypes.FIELD_LIST_ITEM_SEPARATOR; }
    {default_path_delimiter}                                { return ImpExTypes.DEFAULT_PATH_DELIMITER; }
    {alternative_map_delimiter}                             { return ImpExTypes.ALTERNATIVE_MAP_DELIMITER; }
    {default_key_value_delimiter}                           { return ImpExTypes.DEFAULT_KEY_VALUE_DELIMITER; }

    {macro_usage}                                           {
                                                                var macroName = yytext().toString().trim();
                                                                return macroDeclarations.contains(macroName) || macroName.startsWith("$config-")
                                                                    ? ImpExTypes.MACRO_USAGE
                                                                    : ImpExTypes.STRING_LITERAL;
                                                            }

    {string_literal}                                        { return ImpExTypes.STRING_LITERAL; }
    {crlf}                                                  { return ImpExTypes.CRLF; }
}

<FIELD_VALUE> {
    {field_value_prefix_password_encoding}                  { return ImpExTypes.FIELD_VALUE_PASSWORD_ENCODING_PREFIX; }
    "zip:"                                                  { return ImpExTypes.FIELD_VALUE_ZIP_PREFIX; }
    "file:"                                                 { return ImpExTypes.FIELD_VALUE_FILE_PREFIX; }
    "jar:"                                                  { return ImpExTypes.FIELD_VALUE_JAR_PREFIX; }
    "model://"                                              { return ImpExTypes.FIELD_VALUE_SCRIPT_PREFIX; }
    "/medias/"                                              { return ImpExTypes.FIELD_VALUE_EXPLODED_JAR_PREFIX; }
    "http:http"                                             {
                                                                yypushback(4);
                                                                return ImpExTypes.FIELD_VALUE_HTTP_PREFIX;
                                                            }
    {semicolon}                                             { yybegin(FIELD_VALUE_START); return ImpExTypes.FIELD_VALUE_SEPARATOR; }
    {multiline_separator}                                   { return ImpExTypes.MULTILINE_SEPARATOR; }
    {single_quote}                                          { return ImpExTypes.SINGLE_QUOTE; }
    {double_quote}                                          { return ImpExTypes.DOUBLE_QUOTE; }
    {field_value_ignore}                                    { return ImpExTypes.FIELD_VALUE_IGNORE; }
    {field_value_null}                                      { return ImpExTypes.FIELD_VALUE_NULL; }
    {tag_open}                                              { return ImpExTypes.TAG_OPEN; }
    {tag_close}                                             { return ImpExTypes.TAG_CLOSE; }
    {boolean}                                               { return ImpExTypes.BOOLEAN; }
    {digit}                                                 { return ImpExTypes.DIGIT; }

    {comma}                                                 { return ImpExTypes.FIELD_LIST_ITEM_SEPARATOR; }
    {default_path_delimiter}                                { return ImpExTypes.DEFAULT_PATH_DELIMITER; }
    {alternative_map_delimiter}                             { return ImpExTypes.ALTERNATIVE_MAP_DELIMITER; }
    {default_key_value_delimiter}                           { return ImpExTypes.DEFAULT_KEY_VALUE_DELIMITER; }

    {collection_append_prefix}                              { return ImpExTypes.COLLECTION_APPEND_PREFIX; }
    {collection_remove_prefix}                              { return ImpExTypes.COLLECTION_REMOVE_PREFIX; }
    {collection_merge_prefix}                               { return ImpExTypes.COLLECTION_MERGE_PREFIX; }

    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }

    {field_value}                                           { return ImpExTypes.FIELD_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<HEADER_TYPE> {
    {header_type}                                           { yybegin(HEADER_LINE); return ImpExTypes.HEADER_TYPE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<HEADER_LINE> {
    {semicolon}                                             { return ImpExTypes.PARAMETERS_SEPARATOR; }
    {multiline_separator}                                   { return ImpExTypes.MULTILINE_SEPARATOR; }
    {comma}                                                 { return ImpExTypes.COMMA; }
    {dot}                                                   { return ImpExTypes.DOT; }

    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
    {document_id}                                           { return ImpExTypes.DOCUMENT_ID; }
    {parameter_name}{white_space}?+{left_round_bracket}     {
                                                              yybegin(HEADER_LINE);
                                                              yypushback(1);
                                                              return ImpExTypes.FUNCTION;
                                                            }
    {parameter_name}                                        { return ImpExTypes.HEADER_PARAMETER_NAME; }
    {alternative_pattern}                                   { return ImpExTypes.ALTERNATIVE_PATTERN; }
    {special_parameter_marker}                              { yybegin(SPECIAL_PARAMETER); return ImpExTypes.SPECIAL_PARAMETER_MARKER; }
    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpExTypes.ASSIGN_VALUE; }

    {left_round_bracket}                                    { return ImpExTypes.LEFT_ROUND_BRACKET; }
    {right_round_bracket}                                   { return ImpExTypes.RIGHT_ROUND_BRACKET; }

    {left_square_bracket}                                   { yybegin(MODIFIERS_BLOCK); return ImpExTypes.LEFT_SQUARE_BRACKET; }
    {right_square_bracket}                                  { return ImpExTypes.RIGHT_SQUARE_BRACKET; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<SPECIAL_PARAMETER> {
    {special_parameter_end}                                 {
                                                                yybegin(HEADER_LINE);
                                                                yypushback(yylength());
                                                            }
    {special_parameter_value}                               { return ImpExTypes.SPECIAL_PARAMETER_VALUE; }
    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
}

<MODIFIERS_BLOCK> {
    {attribute_name}                                        { return ImpExTypes.ATTRIBUTE_NAME; }

    {assign_value}                                          { yybegin(WAITING_ATTR_OR_PARAM_VALUE); return ImpExTypes.ASSIGN_VALUE; }

    {single_string}                                         { return ImpExTypes.SINGLE_STRING; }
    {double_string}                                         { return ImpExTypes.DOUBLE_STRING; }

    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpExTypes.RIGHT_SQUARE_BRACKET; }

    {comma}                                                 { return ImpExTypes.ATTRIBUTE_SEPARATOR; }

    {alternative_map_delimiter}                             { yybegin(MODIFIERS_BLOCK); return ImpExTypes.ALTERNATIVE_MAP_DELIMITER; }
    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<WAITING_ATTR_OR_PARAM_VALUE> {
    {boolean}                                               { return ImpExTypes.BOOLEAN; }
    {digit}                                                 { return ImpExTypes.DIGIT; }
    {single_string}                                         { return ImpExTypes.SINGLE_STRING; }
    {double_quote}                                          { return ImpExTypes.DOUBLE_QUOTE; }
    {macro_usage}                                           { return ImpExTypes.MACRO_USAGE; }
    {comma}                                                 { yybegin(MODIFIERS_BLOCK); return ImpExTypes.ATTRIBUTE_SEPARATOR; }
    {attribute_value}                                       { return ImpExTypes.ATTRIBUTE_VALUE; }
    {right_square_bracket}                                  { yybegin(HEADER_LINE); return ImpExTypes.RIGHT_SQUARE_BRACKET; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<MACRO_DECLARATION> {
    {assign_value}                                          { yybegin(WAITING_MACRO_VALUE); return ImpExTypes.ASSIGN_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<WAITING_MACRO_VALUE> {
    {single_quote}                                          { return ImpExTypes.SINGLE_QUOTE; }
    {double_quote}                                          { return ImpExTypes.DOUBLE_QUOTE; }

    {macro_usage}                                           { yypushback(yylength()); yybegin(WAITING_MACRO_CONFIG_USAGE); }

    {left_round_bracket}                                    { return ImpExTypes.LEFT_ROUND_BRACKET; }
    {right_round_bracket}                                   { return ImpExTypes.RIGHT_ROUND_BRACKET; }

    {assign_value}                                          { return ImpExTypes.ASSIGN_VALUE; }

    {left_square_bracket}                                   { return ImpExTypes.LEFT_SQUARE_BRACKET; }
    {right_square_bracket}                                  { return ImpExTypes.RIGHT_SQUARE_BRACKET; }

    {boolean}                                               { return ImpExTypes.BOOLEAN; }
    {digit}                                                 { return ImpExTypes.DIGIT; }
    {field_value_ignore}                                    { return ImpExTypes.FIELD_VALUE_IGNORE; }
    {field_value_null}                                      { return ImpExTypes.FIELD_VALUE_NULL; }

    {comma}                                                 { return ImpExTypes.COMMA; }

    {header_mode_insert}                                    { return ImpExTypes.HEADER_MODE_INSERT; }
    {header_mode_update}                                    { return ImpExTypes.HEADER_MODE_UPDATE; }
    {header_mode_insert_update}                             { return ImpExTypes.HEADER_MODE_INSERT_UPDATE; }
    {header_mode_remove}                                    { return ImpExTypes.HEADER_MODE_REMOVE; }

    {macro_value}                                           { return ImpExTypes.MACRO_VALUE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<WAITING_MACRO_CONFIG_USAGE> {
    {macro_config_usage}                                    { yybegin(WAITING_MACRO_VALUE); return ImpExTypes.MACRO_USAGE; }
    {crlf}                                                  { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
   .                                                        { yypushback(yylength()); yybegin(MACRO_USAGE); }
}

<MACRO_USAGE> {
    {macro_usage}   {
                        var candidate = yytext().toString();

                        // Exact match — declared variable
                        if (macroDeclarations.contains(candidate)) {
                            yybegin(WAITING_MACRO_VALUE);
                            return ImpExTypes.MACRO_USAGE;
                        }

                        // Find longest declared prefix
                        String bestMatch = null;
                        for (final String declared : macroDeclarations) {
                            if (candidate.startsWith(declared)) {
                                if (bestMatch == null || declared.length() > bestMatch.length()) {
                                    bestMatch = declared;
                                }
                            }
                        }

                        if (bestMatch != null) {
                            // Push back the suffix (the non-variable remainder)
                            yypushback(candidate.length() - bestMatch.length());
                            yybegin(MACRO_USAGE_CANDIDATE);
                            return ImpExTypes.MACRO_USAGE;
                        }

                        // No declared variable matched — treat as plain macro value
                        yybegin(WAITING_MACRO_VALUE);
                        return ImpExTypes.MACRO_VALUE;
                    }
    {crlf}          { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

<MACRO_USAGE_CANDIDATE> {
    {macro_value}   { yybegin(WAITING_MACRO_VALUE); return ImpExTypes.MACRO_VALUE; }
    {crlf}          { yybegin(YYINITIAL); return ImpExTypes.CRLF; }
}

// Fallback
.                                                           { return TokenType.BAD_CHARACTER; }
