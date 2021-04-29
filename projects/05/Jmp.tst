load Jmp.hdl,
output-file Jmp.out,
compare-to Jmp.cmp,
output-list j1%B1.1.1 j2%B1.1.1 j3%B1.1.1 zr%B1.1.1 ng%B1.1.1 out%B1.1.1;

set j1 0, set j2 0, set  j3 1,  set zr 0,  set ng 0,
eval,
output;

set j1 0, set j2 0, set  j3 1,  set zr 1,  set ng 0,
eval,
output;

set j1 0, set j2 1, set  j3 0,  set zr 1,  set ng 0,
eval,
output;

set j1 0, set j2 1, set  j3 0,  set zr 1,  set ng 1,
eval,
output;

set j1 0, set j2 1, set  j3 1,  set zr 1,  set ng 0,
eval,
output;

set j1 0, set j2 1, set  j3 1,  set zr 0,  set ng 1,
eval,
output;

set j1 1, set j2 0, set  j3 0,  set zr 0,  set ng 1,
eval,
output;

set j1 1, set j2 0, set  j3 0,  set zr 1,  set ng 1,
eval,
output;


set j1 1, set j2 0, set  j3 1,  set zr 0,  set ng 0,
eval,
output;

set j1 1, set j2 0, set  j3 1,  set zr 0,  set ng 1,
eval,
output;

set j1 1, set j2 1, set  j3 0,  set zr 1,  set ng 1,
eval,
output;

set j1 1, set j2 1, set  j3 0,  set zr 1,  set ng 0,
eval,
output;

set j1 1, set j2 1, set  j3 0,  set zr 0,  set ng 1,
eval,
output;


set j1 1, set j2 1, set  j3 1,  set zr 1,  set ng 1,
eval,
output;


set j1  0 , set j2  0 , set j3  0 , set zr  0 , set ng  0,  
eval,
output;

set j1  0 , set j2  0 , set j3  0 , set zr  0 , set ng  1,
eval,
output;

set j1  0 , set j2  0 , set j3  0 , set zr  1 , set ng  0,  
eval,
output;

set j1  0 , set j2  0 , set j3  0 , set zr  1 , set ng  1, 
eval,
output;

set j1  0 , set j2  0 , set j3  1 , set zr  0 , set ng  1,  
eval,
output;

set j1  0 , set j2  0 , set j3  1 , set zr  1 , set ng  1,  
eval,
output;

set j1  0 , set j2  1 , set j3  0 , set zr  0 , set ng  0,  
eval,
output;

set j1  0 , set j2  1 , set j3  0 , set zr  0 , set ng  1,  
eval,
output;

set j1  0 , set j2  1 , set j3  1 , set zr  0 , set ng  0,  
eval,
output;

set j1  1 , set j2  0 , set j3  0 , set zr  0 , set ng  0,  
eval,
output;

set j1  1 , set j2  0 , set j3  0 , set zr  1 , set ng  0,  
eval,
output;

set j1  1 , set j2  0 , set j3  1 , set zr  1 , set ng  0, 
eval,
output;

set j1  1 , set j2  0 , set j3  1 , set zr  1 , set ng  1,  
eval,
output;

set j1  1 , set j2  1 , set j3  0 , set zr  0 , set ng  0,  
eval,
output;

set j1  1 , set j2  1 , set j3  1 , set zr  0 , set ng  0,  
eval,
output;

set j1  1 , set j2  1 , set j3  1 , set zr  0 , set ng  1,  
eval,
output;

set j1  1 , set j2  1 , set j3  1 , set zr  1 , set ng  0,  
eval,
output;
