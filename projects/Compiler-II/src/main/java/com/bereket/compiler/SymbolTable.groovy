package com.bereket.compiler

class SymbolTable {

    private Map<String, SymbolEntry> classScopeMap = new HashMap<>();
    private Map<String, SymbolEntry> subroutineScopeMap = new HashMap<>();


    SymbolTable(){}

    public void define(String name, String type, IdentifierKind kind){

        Map<String, SymbolEntry> scopedMap = getScopeMapFromKind(kind)
        if(scopedMap.containsKey(name)) return
        int count = varCount(kind) //if count = 1, the first element index will be 0. Thus the next index = count
        SymbolEntry symbolEntry = new SymbolEntry(type, kind, count) //the next index = count
        scopedMap.put(name, symbolEntry)
    }

    public int varCount(IdentifierKind kind){
        Map<String, SymbolEntry> map = (kind in [IdentifierKind.STATIC, IdentifierKind.FIELD]) ? classScopeMap : subroutineScopeMap
        return map.entrySet().count {e -> ((SymbolEntry)e.value).kind == kind}
    }

    public IdentifierKind kindOf(String identifier){
        SymbolEntry entry = getSymbolEntry(identifier)
        if(entry) return entry.kind
        return IdentifierKind.NONE
    }

    public String typeOf(String identifier){
        SymbolEntry entry = getSymbolEntry(identifier, true)
        return entry.type
    }

    public int indexOf(String identifier){
        SymbolEntry entry = getSymbolEntry(identifier, true)
        return entry.index
    }

    //method first looks the identifier in subroutine scope, then in class scope
    SymbolEntry getSymbolEntry(String identifier, boolean throwIfNotFound = false){
        SymbolEntry entry = subroutineScopeMap.get(identifier)
        if(entry) return entry
        entry = classScopeMap.get(identifier)
        if(!entry){
            if(throwIfNotFound) throw new RuntimeException("Identifier is not found")
            return null
        }
        return entry
    }

    private Map<String, SymbolEntry> getScopeMapFromKind(IdentifierKind kind){
        if(kind == IdentifierKind.NONE) throw new RuntimeException("Cannot define an identifier of kind '${kind}'")
        return (kind in [IdentifierKind.STATIC, IdentifierKind.FIELD]) ? classScopeMap : subroutineScopeMap
    }

    public static class SymbolEntry {
        String type
        IdentifierKind kind
        int index;

        SymbolEntry(String type, IdentifierKind kind, int index = 0){
            this.type = type
            this.kind = kind
            this.index = index
        }
    }

    public void restSubRoutineSymbolTable(){
        this.subroutineScopeMap = new HashMap<>()
    }

    public int countSubroutineVariables(){
        return subroutineScopeMap.keySet().size()
    }

    public int countClassVariables(){
        return classScopeMap.keySet().size()
    }
}