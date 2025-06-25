package me.hasenzahn1.homemanager.util;

/**
 * A class that evaluates mathematical expressions represented as strings.
 * This class supports basic arithmetic operations such as addition, subtraction, multiplication, division,
 * exponentiation, as well as common mathematical functions like sqrt, sin, cos, and tan.
 */
public class ExpressionEvaluator {

    /**
     * Evaluates the given mathematical expression in the form of a string.
     *
     * @param str the mathematical expression to evaluate
     * @return the result of the evaluation as a double
     * @throws RuntimeException if the expression is malformed or contains unknown characters
     */
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            /**
             * Advances to the next character in the expression.
             */
            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            /**
             * Attempts to consume a specific character if it matches the current character.
             * Skips whitespace characters.
             *
             * @param charToEat the character to match and consume
             * @return true if the character was consumed, false otherwise
             */
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            /**
             * Parses the entire expression and returns the result.
             *
             * @return the evaluated result of the expression
             * @throws RuntimeException if there is an unexpected character in the expression
             */
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            /**
             * Parses an expression, which can consist of terms and addition or subtraction operations.
             *
             * @return the result of the parsed expression
             */
            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            /**
             * Parses a term, which can consist of factors and multiplication or division operations.
             *
             * @return the result of the parsed term
             */
            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            /**
             * Parses a factor, which can be a number, a parenthesized expression, or a function.
             *
             * @return the result of the parsed factor
             * @throws RuntimeException if there is an invalid factor in the expression
             */
            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    x = switch (func) {
                        case "sqrt" -> Math.sqrt(x);
                        case "sin" -> Math.sin(Math.toRadians(x));
                        case "cos" -> Math.cos(Math.toRadians(x));
                        case "tan" -> Math.tan(Math.toRadians(x));
                        default -> throw new RuntimeException("Unknown function: " + func);
                    };
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}
