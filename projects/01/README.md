# PROJECT 1

This project implements basic logic gates from NAND (not-and) gates.

## File naming:
xxx.hdl	$\to$ basic logic gate <br>
xxxyy.hdl $\to$ logic gate for yy-long input streams <br>
xxxzzWay.hdl $\to$ logic gate for zz different input streams<br>


## Gates:
### And 
requires 2 input values
returns true if <b> both </b> input Values are true
### Or
requires 2 input values
returns true if <b> any </b> input Values are true
### Not
requires 1 input values
negates input
### Xor
requires 2 input values
returns true if <b> only one </b> input Values are true
### Mux 
requires 2 input values and a selection bit
returns one of the input value according to selection bit (0 $\to$ 1st input, 1 $\to$ 2nd input)
### DMux
requires 1 input value and selection bit
outputs 2 values
returns input to selected output (all other outputs are set to false)