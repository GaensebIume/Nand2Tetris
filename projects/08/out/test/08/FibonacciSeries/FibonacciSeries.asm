
//push argument 1
//pop pointer 1           
//push constant 0
//pop that 0              
//push constant 1
//pop that 1              
//push argument 0
//push constant 2
//sub
//pop argument 0          
//label MAIN_LOOP_START
//push argument 0
//if-goto COMPUTE_ELEMENT 
//goto END_PROGRAM        
//label COMPUTE_ELEMENT
//push that 0
//push that 1
//add
//pop that 2              
//push pointer 1
//push constant 1
//add
//pop pointer 1           
//push argument 0
//push constant 1
//sub
//pop argument 0          
//goto MAIN_LOOP_START
//label END_PROGRAM