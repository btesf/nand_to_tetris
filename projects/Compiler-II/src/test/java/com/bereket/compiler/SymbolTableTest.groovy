package com.bereket.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymbolTableTest {

    SymbolTable symbolTable;

    @BeforeEach
    void setUp() {
        symbolTable = new SymbolTable();
    }

    @Test
    void define() {
        symbolTable.define("a", "int", IdentifierKind.FIELD);
        symbolTable.define("a", "int", IdentifierKind.VAR);
        symbolTable.define("a", "int", IdentifierKind.VAR); //repeat the same thing; This should be ignored
        symbolTable.define("b", "int", IdentifierKind.STATIC);

        SymbolTable.SymbolEntry entry = symbolTable.getSymbolEntry("a");
        //the inner scope contains "a". Thus the the variable with the type VAR should be returned
        assertEquals(entry.getKind(), IdentifierKind.VAR);
        entry = symbolTable.getSymbolEntry("b");
        assertEquals(entry.getKind(), IdentifierKind.STATIC);
    }

    @Test
    void varCount() {

        symbolTable.define("a", "int", IdentifierKind.FIELD);
        symbolTable.define("a", "int", IdentifierKind.VAR)

        assertEquals(symbolTable.varCount(IdentifierKind.VAR), 1);
        assertEquals(symbolTable.varCount(IdentifierKind.FIELD), 1);
    }

    @Test
    void kindOf() {

        symbolTable.define("a", "int", IdentifierKind.FIELD);
        assertEquals(symbolTable.kindOf("a"), IdentifierKind.FIELD);
    }

    @Test
    void typeOf() {
        symbolTable.define("a", "int", IdentifierKind.FIELD);
        assertEquals(symbolTable.typeOf("a"), "int");
    }

    @Test
    void indexOf() {
        symbolTable.define("a", "int", IdentifierKind.FIELD);
        symbolTable.define("b", "int", IdentifierKind.FIELD);
        symbolTable.define("c", "int", IdentifierKind.FIELD);
        assertEquals(symbolTable.indexOf("c"), 2);
    }

    @Test
    void restSubRoutineSymbolTable() {
        symbolTable.define("a", "int", IdentifierKind.VAR);
        symbolTable.define("b", "int", IdentifierKind.VAR);
        symbolTable.define("c", "int", IdentifierKind.VAR);
        symbolTable.define("d", "int", IdentifierKind.STATIC);
        assertEquals(symbolTable.varCount(IdentifierKind.VAR), 3);
        assertEquals(symbolTable.varCount(IdentifierKind.STATIC), 1);
        symbolTable.restSubRoutineSymbolTable()
        assertEquals(symbolTable.varCount(IdentifierKind.VAR), 0);
        assertEquals(symbolTable.varCount(IdentifierKind.STATIC), 1);
    }
}