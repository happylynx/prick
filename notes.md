## internal commands

* export <revision> [subtree] output-directory
    * exports
* sync root1 root2 
    * root is a dir containing `.prick` directory
    * root can be
        * bare (no working dir, full or pruned history) or
        * shallow (working dir but no history files, only history metadata)
* config
    * options
        * metadata-count-limit [positive int or unset][default unset][unit 1]
            * not more metadata checkpoints than this count will be stored
        * metadata-period-limit [list(comma separated) of ascending positive ints or unset][default unset][unit list of days]
            * instructs prick to delete all metadata points except for the oldest one in the interval, last number is understood
              as repeating indefinitely
    * subcommands
        * get
        * set
* prune commit [commit ...]
    * removes selected commits from history
* prunedata commit [commit ...]
    * converts commits to metadata commits
* verify [dir] [store]
    * verifies hashes of woring *dir* or hash *store*
* list [revision] [subtree]
    * lists file entries certain revision and subtree
    * if subtree is omitted, it lists files in working dir; revision is required for bare repos
* setdir [-p] <revision> [subtree]
    * sets subtree in working directory to match selected revision
    * -p, --pure removes files that are not part of the revision
* init (--bare|--shallow) [dir]
* gc
    * it cleans the store

## dictionary

* revision - hash of either commit or tree object