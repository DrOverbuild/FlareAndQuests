# DrizzardFlareAndQuests

Repository for Rank Quests and Flares plugin

## Code Style Guidelines
* Use tabs rather than spaces to indent.
* Left Curly braces of code blocks start on the same line as the header.
* Always use braces with `if`, `else`, `for`, and `while` while statements even if the code in the block is one line, unless the code is not too long so that it can stay on the same line as the statement. For example, this:

  ```
  if (item == null)
     return;
  ```
  
  would be illegal while this:
  
  ```
  if (item == null) {
     return;
  }
  ```
  
  or this:
  
  ```
  if (item == null) return;
  ```
  
  would be legal.
