# XTF utilities for radanalytics.io related projects

## Contributors guide

* Fork the project
* Create your local development brunch
* Rebase local development branch on upstream/master\
``git checkout -b local_development_branch_name && git rebase upstream/master``
* Commit your changes by next rule:\
** each commit should include only his changes scope\
** don't use \#.1 or 1. in commit message 
* use next command to combine commits by changes scope:\
  ```
  $ git fetch upstream
  $ git checkout local_development_branch_name 
  $ git rebase -i upstream/master
  
  < choose squash for all of your commits, except the first one >
  < Edit the commit message to make sense, and describe all your changes >
  
  $ git push origin local_development_branch_name -f
  ```
  


  