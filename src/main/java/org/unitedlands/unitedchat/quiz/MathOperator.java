package org.unitedlands.unitedchat.quiz;

public enum MathOperator {

    ADD("+") {
        int calculate(int a, int b) {
            return a + b;
        }
    },

    SUB("-") {
        int calculate(int a, int b) {
            return a - b;
        }
    },

    MUL("•") {
        int calculate(int a, int b) {
            return a * b;
        }
    },

    DIV(":") {
        int calculate(int a, int b) {
            return a / b;
        }
    };

    final String symbol;

    MathOperator(String symbol) {
        this.symbol = symbol;
    }

    abstract int calculate(int a, int b);

}
