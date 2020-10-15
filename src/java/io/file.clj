(ns java.io.file
  (:refer-clojure :exclude [list])
  (:require [tortilla.wrap]))

;; ==== java.io.File ====

(clojure.core/defn can-execute
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5008]
   (clojure.core/if-let [[p0_5008] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "canExecute"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5008]))]
     (.canExecute ^{:tag java.io.File} p0_5008)
     (tortilla.wrap/type-error "java.io.File::canExecute" p0_5008))))

(clojure.core/defn can-read
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5010]
   (clojure.core/if-let [[p0_5010] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "canRead"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5010]))]
     (.canRead ^{:tag java.io.File} p0_5010)
     (tortilla.wrap/type-error "java.io.File::canRead" p0_5010))))

(clojure.core/defn can-write
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5012]
   (clojure.core/if-let [[p0_5012] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "canWrite"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5012]))]
     (.canWrite ^{:tag java.io.File} p0_5012)
     (tortilla.wrap/type-error "java.io.File::canWrite" p0_5012))))

(clojure.core/defn compare-to
  {:arglists '([java.io.File java.io.File] [java.io.File java.lang.Object])}
  (^{:tag java.lang.Integer}
   [p0_5014 p1_5015]
   (clojure.core/let [args_5016 [p0_5014 p1_5015]
                      [id__3793__auto__ p0_5014 p1_5015] (tortilla.wrap/select-overload
                                                          args_5016
                                                          [(tortilla.wrap/args-compatible
                                                            (tortilla.wrap/->MemberInfo
                                                             0
                                                             :method
                                                             "compareTo"
                                                             java.io.File
                                                             java.lang.Integer/TYPE
                                                             [java.lang.Object]
                                                             1
                                                             #{}
                                                             nil)
                                                            args_5016)
                                                           (tortilla.wrap/args-compatible
                                                            (tortilla.wrap/->MemberInfo
                                                             1
                                                             :method
                                                             "compareTo"
                                                             java.io.File
                                                             java.lang.Integer/TYPE
                                                             [java.io.File]
                                                             1
                                                             #{}
                                                             nil)
                                                            args_5016)])]
     (clojure.core/case (clojure.core/long id__3793__auto__)
       0 (.compareTo ^{:tag java.io.File} p0_5014 ^{:tag java.lang.Object} p1_5015)
       1 (.compareTo ^{:tag java.io.File} p0_5014 ^{:tag java.io.File} p1_5015)
       (tortilla.wrap/type-error "java.io.File::compareTo" p0_5014 p1_5015)))))

(clojure.core/defn create-new-file
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5017]
   (clojure.core/if-let [[p0_5017] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "createNewFile"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5017]))]
     (.createNewFile ^{:tag java.io.File} p0_5017)
     (tortilla.wrap/type-error "java.io.File::createNewFile" p0_5017))))

(clojure.core/defn create-temp-file
  {:arglists '([java.lang.String java.lang.String] [java.lang.String java.lang.String java.io.File])}
  (^{:tag java.io.File}
   [p0_5019 p1_5020]
   (clojure.core/if-let [[p0_5019 p1_5020] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "createTempFile"
                                              java.io.File
                                              java.io.File
                                              [java.lang.String java.lang.String]
                                              2
                                              #{:static}
                                              nil)
                                             [p0_5019 p1_5020]))]
     (java.io.File/createTempFile ^{:tag java.lang.String} p0_5019 ^{:tag java.lang.String} p1_5020)
     (tortilla.wrap/type-error "java.io.File::createTempFile" p0_5019 p1_5020)))
  (^{:tag java.io.File}
   [p0_5022 p1_5023 p2_5024]
   (clojure.core/if-let [[p0_5022 p1_5023 p2_5024] (:invocation-args
                                                    (tortilla.wrap/args-compatible
                                                     (tortilla.wrap/->MemberInfo
                                                      -1
                                                      :method
                                                      "createTempFile"
                                                      java.io.File
                                                      java.io.File
                                                      [java.lang.String
                                                       java.lang.String
                                                       java.io.File]
                                                      3
                                                      #{:static}
                                                      nil)
                                                     [p0_5022 p1_5023 p2_5024]))]
     (java.io.File/createTempFile
^{:tag java.lang.String}
p0_5022
^{:tag java.lang.String}
p1_5023
^{:tag java.io.File}
p2_5024)
     (tortilla.wrap/type-error "java.io.File::createTempFile" p0_5022 p1_5023 p2_5024))))

(clojure.core/defn delete
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5026]
   (clojure.core/if-let [[p0_5026] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "delete"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5026]))]
     (.delete ^{:tag java.io.File} p0_5026)
     (tortilla.wrap/type-error "java.io.File::delete" p0_5026))))

(clojure.core/defn delete-on-exit
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Object}
   [p0_5028]
   (clojure.core/if-let [[p0_5028] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "deleteOnExit"
                                      java.io.File
                                      java.lang.Void/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5028]))]
     (.deleteOnExit ^{:tag java.io.File} p0_5028)
     (tortilla.wrap/type-error "java.io.File::deleteOnExit" p0_5028))))

(clojure.core/defn equals
  {:arglists '([java.io.File java.lang.Object])}
  (^{:tag java.lang.Boolean}
   [p0_5030 p1_5031]
   (clojure.core/if-let [[p0_5030 p1_5031] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "equals"
                                              java.io.File
                                              java.lang.Boolean/TYPE
                                              [java.lang.Object]
                                              1
                                              #{}
                                              nil)
                                             [p0_5030 p1_5031]))]
     (.equals ^{:tag java.io.File} p0_5030 ^{:tag java.lang.Object} p1_5031)
     (tortilla.wrap/type-error "java.io.File::equals" p0_5030 p1_5031))))

(clojure.core/defn exists
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5033]
   (clojure.core/if-let [[p0_5033] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "exists"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5033]))]
     (.exists ^{:tag java.io.File} p0_5033)
     (tortilla.wrap/type-error "java.io.File::exists" p0_5033))))

(clojure.core/defn file
  {:arglists '([java.lang.String]
               [java.net.URI]
               [java.io.File java.lang.String]
               [java.lang.String java.lang.String])}
  (^{:tag java.io.File}
   [p0_5035]
   (clojure.core/let [args_5036 [p0_5035]
                      [id__3793__auto__ p0_5035] (tortilla.wrap/select-overload
                                                  args_5036
                                                  [(tortilla.wrap/args-compatible
                                                    (tortilla.wrap/->MemberInfo
                                                     0
                                                     :constructor
                                                     "File"
                                                     java.io.File
                                                     java.io.File
                                                     [java.lang.String]
                                                     1
                                                     #{}
                                                     nil)
                                                    args_5036)
                                                   (tortilla.wrap/args-compatible
                                                    (tortilla.wrap/->MemberInfo
                                                     1
                                                     :constructor
                                                     "File"
                                                     java.io.File
                                                     java.io.File
                                                     [java.net.URI]
                                                     1
                                                     #{}
                                                     nil)
                                                    args_5036)])]
     (clojure.core/case (clojure.core/long id__3793__auto__)
       0 (java.io.File. ^{:tag java.lang.String} p0_5035)
       1 (java.io.File. ^{:tag java.net.URI} p0_5035)
       (tortilla.wrap/type-error "java.io.File::File" p0_5035))))
  (^{:tag java.io.File}
   [p0_5037 p1_5038]
   (clojure.core/let [args_5039 [p0_5037 p1_5038]
                      [id__3793__auto__ p0_5037 p1_5038] (tortilla.wrap/select-overload
                                                          args_5039
                                                          [(tortilla.wrap/args-compatible
                                                            (tortilla.wrap/->MemberInfo
                                                             0
                                                             :constructor
                                                             "File"
                                                             java.io.File
                                                             java.io.File
                                                             [java.lang.String java.lang.String]
                                                             2
                                                             #{}
                                                             nil)
                                                            args_5039)
                                                           (tortilla.wrap/args-compatible
                                                            (tortilla.wrap/->MemberInfo
                                                             1
                                                             :constructor
                                                             "File"
                                                             java.io.File
                                                             java.io.File
                                                             [java.io.File java.lang.String]
                                                             2
                                                             #{}
                                                             nil)
                                                            args_5039)])]
     (clojure.core/case (clojure.core/long id__3793__auto__)
       0 (java.io.File. ^{:tag java.lang.String} p0_5037 ^{:tag java.lang.String} p1_5038)
       1 (java.io.File. ^{:tag java.io.File} p0_5037 ^{:tag java.lang.String} p1_5038)
       (tortilla.wrap/type-error "java.io.File::File" p0_5037 p1_5038)))))

(clojure.core/defn get-absolute-file
  {:arglists '([java.io.File])}
  (^{:tag java.io.File}
   [p0_5040]
   (clojure.core/if-let [[p0_5040] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getAbsoluteFile"
                                      java.io.File
                                      java.io.File
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5040]))]
     (.getAbsoluteFile ^{:tag java.io.File} p0_5040)
     (tortilla.wrap/type-error "java.io.File::getAbsoluteFile" p0_5040))))

(clojure.core/defn get-absolute-path
  {:arglists '([java.io.File])}
  (^{:tag java.lang.String}
   [p0_5042]
   (clojure.core/if-let [[p0_5042] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getAbsolutePath"
                                      java.io.File
                                      java.lang.String
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5042]))]
     (.getAbsolutePath ^{:tag java.io.File} p0_5042)
     (tortilla.wrap/type-error "java.io.File::getAbsolutePath" p0_5042))))

(clojure.core/defn get-canonical-file
  {:arglists '([java.io.File])}
  (^{:tag java.io.File}
   [p0_5044]
   (clojure.core/if-let [[p0_5044] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getCanonicalFile"
                                      java.io.File
                                      java.io.File
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5044]))]
     (.getCanonicalFile ^{:tag java.io.File} p0_5044)
     (tortilla.wrap/type-error "java.io.File::getCanonicalFile" p0_5044))))

(clojure.core/defn get-canonical-path
  {:arglists '([java.io.File])}
  (^{:tag java.lang.String}
   [p0_5046]
   (clojure.core/if-let [[p0_5046] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getCanonicalPath"
                                      java.io.File
                                      java.lang.String
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5046]))]
     (.getCanonicalPath ^{:tag java.io.File} p0_5046)
     (tortilla.wrap/type-error "java.io.File::getCanonicalPath" p0_5046))))

(clojure.core/defn get-free-space
  {:arglists '([java.io.File])}
  (^{:tag long}
   [p0_5048]
   (clojure.core/if-let [[p0_5048] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getFreeSpace"
                                      java.io.File
                                      java.lang.Long/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5048]))]
     (.getFreeSpace ^{:tag java.io.File} p0_5048)
     (tortilla.wrap/type-error "java.io.File::getFreeSpace" p0_5048))))

(clojure.core/defn get-name
  {:arglists '([java.io.File])}
  (^{:tag java.lang.String}
   [p0_5050]
   (clojure.core/if-let [[p0_5050] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getName"
                                      java.io.File
                                      java.lang.String
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5050]))]
     (.getName ^{:tag java.io.File} p0_5050)
     (tortilla.wrap/type-error "java.io.File::getName" p0_5050))))

(clojure.core/defn get-parent
  {:arglists '([java.io.File])}
  (^{:tag java.lang.String}
   [p0_5052]
   (clojure.core/if-let [[p0_5052] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getParent"
                                      java.io.File
                                      java.lang.String
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5052]))]
     (.getParent ^{:tag java.io.File} p0_5052)
     (tortilla.wrap/type-error "java.io.File::getParent" p0_5052))))

(clojure.core/defn get-parent-file
  {:arglists '([java.io.File])}
  (^{:tag java.io.File}
   [p0_5054]
   (clojure.core/if-let [[p0_5054] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getParentFile"
                                      java.io.File
                                      java.io.File
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5054]))]
     (.getParentFile ^{:tag java.io.File} p0_5054)
     (tortilla.wrap/type-error "java.io.File::getParentFile" p0_5054))))

(clojure.core/defn get-path
  {:arglists '([java.io.File])}
  (^{:tag java.lang.String}
   [p0_5056]
   (clojure.core/if-let [[p0_5056] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getPath"
                                      java.io.File
                                      java.lang.String
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5056]))]
     (.getPath ^{:tag java.io.File} p0_5056)
     (tortilla.wrap/type-error "java.io.File::getPath" p0_5056))))

(clojure.core/defn get-total-space
  {:arglists '([java.io.File])}
  (^{:tag long}
   [p0_5058]
   (clojure.core/if-let [[p0_5058] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getTotalSpace"
                                      java.io.File
                                      java.lang.Long/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5058]))]
     (.getTotalSpace ^{:tag java.io.File} p0_5058)
     (tortilla.wrap/type-error "java.io.File::getTotalSpace" p0_5058))))

(clojure.core/defn get-usable-space
  {:arglists '([java.io.File])}
  (^{:tag long}
   [p0_5060]
   (clojure.core/if-let [[p0_5060] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "getUsableSpace"
                                      java.io.File
                                      java.lang.Long/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5060]))]
     (.getUsableSpace ^{:tag java.io.File} p0_5060)
     (tortilla.wrap/type-error "java.io.File::getUsableSpace" p0_5060))))

(clojure.core/defn hash-code
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Integer}
   [p0_5062]
   (clojure.core/if-let [[p0_5062] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "hashCode"
                                      java.io.File
                                      java.lang.Integer/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5062]))]
     (.hashCode ^{:tag java.io.File} p0_5062)
     (tortilla.wrap/type-error "java.io.File::hashCode" p0_5062))))

(clojure.core/defn is-absolute
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5064]
   (clojure.core/if-let [[p0_5064] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "isAbsolute"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5064]))]
     (.isAbsolute ^{:tag java.io.File} p0_5064)
     (tortilla.wrap/type-error "java.io.File::isAbsolute" p0_5064))))

(clojure.core/defn is-directory
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5066]
   (clojure.core/if-let [[p0_5066] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "isDirectory"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5066]))]
     (.isDirectory ^{:tag java.io.File} p0_5066)
     (tortilla.wrap/type-error "java.io.File::isDirectory" p0_5066))))

(clojure.core/defn is-file
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5068]
   (clojure.core/if-let [[p0_5068] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "isFile"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5068]))]
     (.isFile ^{:tag java.io.File} p0_5068)
     (tortilla.wrap/type-error "java.io.File::isFile" p0_5068))))

(clojure.core/defn is-hidden
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5070]
   (clojure.core/if-let [[p0_5070] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "isHidden"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5070]))]
     (.isHidden ^{:tag java.io.File} p0_5070)
     (tortilla.wrap/type-error "java.io.File::isHidden" p0_5070))))

(clojure.core/defn last-modified
  {:arglists '([java.io.File])}
  (^{:tag long}
   [p0_5072]
   (clojure.core/if-let [[p0_5072] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "lastModified"
                                      java.io.File
                                      java.lang.Long/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5072]))]
     (.lastModified ^{:tag java.io.File} p0_5072)
     (tortilla.wrap/type-error "java.io.File::lastModified" p0_5072))))

(clojure.core/defn length
  {:arglists '([java.io.File])}
  (^{:tag long}
   [p0_5074]
   (clojure.core/if-let [[p0_5074] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "length"
                                      java.io.File
                                      java.lang.Long/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5074]))]
     (.length ^{:tag java.io.File} p0_5074)
     (tortilla.wrap/type-error "java.io.File::length" p0_5074))))

(clojure.core/defn list
  {:arglists '([java.io.File] [java.io.File java.io.FilenameFilter])}
  (^{:tag "[Ljava.lang.String;"}
   [p0_5076]
   (clojure.core/if-let [[p0_5076] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "list"
                                      java.io.File
                                      (tortilla.wrap/array-of java.lang.String)
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5076]))]
     (.list ^{:tag java.io.File} p0_5076)
     (tortilla.wrap/type-error "java.io.File::list" p0_5076)))
  (^{:tag "[Ljava.lang.String;"}
   [p0_5078 p1_5079]
   (clojure.core/if-let [[p0_5078 p1_5079] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "list"
                                              java.io.File
                                              (tortilla.wrap/array-of java.lang.String)
                                              [java.io.FilenameFilter]
                                              1
                                              #{}
                                              nil)
                                             [p0_5078 p1_5079]))]
     (.list ^{:tag java.io.File} p0_5078 ^{:tag java.io.FilenameFilter} p1_5079)
     (tortilla.wrap/type-error "java.io.File::list" p0_5078 p1_5079))))

(clojure.core/defn list-files
  {:arglists '([java.io.File] [java.io.File java.io.FileFilter] [java.io.File java.io.FilenameFilter])}
  (^{:tag "[Ljava.io.File;"}
   [p0_5081]
   (clojure.core/if-let [[p0_5081] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "listFiles"
                                      java.io.File
                                      (tortilla.wrap/array-of java.io.File)
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5081]))]
     (.listFiles ^{:tag java.io.File} p0_5081)
     (tortilla.wrap/type-error "java.io.File::listFiles" p0_5081)))
  (^{:tag "[Ljava.io.File;"}
   [p0_5083 p1_5084]
   (clojure.core/let [args_5085 [p0_5083 p1_5084]
                      [id__3793__auto__ p0_5083 p1_5084] (tortilla.wrap/select-overload
                                                          args_5085
                                                          [(tortilla.wrap/args-compatible
                                                            (tortilla.wrap/->MemberInfo
                                                             0
                                                             :method
                                                             "listFiles"
                                                             java.io.File
                                                             (tortilla.wrap/array-of java.io.File)
                                                             [java.io.FilenameFilter]
                                                             1
                                                             #{}
                                                             nil)
                                                            args_5085)
                                                           (tortilla.wrap/args-compatible
                                                            (tortilla.wrap/->MemberInfo
                                                             1
                                                             :method
                                                             "listFiles"
                                                             java.io.File
                                                             (tortilla.wrap/array-of java.io.File)
                                                             [java.io.FileFilter]
                                                             1
                                                             #{}
                                                             nil)
                                                            args_5085)])]
     (clojure.core/case (clojure.core/long id__3793__auto__)
       0 (.listFiles ^{:tag java.io.File} p0_5083 ^{:tag java.io.FilenameFilter} p1_5084)
       1 (.listFiles ^{:tag java.io.File} p0_5083 ^{:tag java.io.FileFilter} p1_5084)
       (tortilla.wrap/type-error "java.io.File::listFiles" p0_5083 p1_5084)))))

(clojure.core/defn list-roots
  {:arglists '([])}
  (^{:tag "[Ljava.io.File;"} [] (java.io.File/listRoots)))

(clojure.core/defn mkdir
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5087]
   (clojure.core/if-let [[p0_5087] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "mkdir"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5087]))]
     (.mkdir ^{:tag java.io.File} p0_5087)
     (tortilla.wrap/type-error "java.io.File::mkdir" p0_5087))))

(clojure.core/defn mkdirs
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5089]
   (clojure.core/if-let [[p0_5089] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "mkdirs"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5089]))]
     (.mkdirs ^{:tag java.io.File} p0_5089)
     (tortilla.wrap/type-error "java.io.File::mkdirs" p0_5089))))

(clojure.core/defn rename-to
  {:arglists '([java.io.File java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5091 p1_5092]
   (clojure.core/if-let [[p0_5091 p1_5092] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "renameTo"
                                              java.io.File
                                              java.lang.Boolean/TYPE
                                              [java.io.File]
                                              1
                                              #{}
                                              nil)
                                             [p0_5091 p1_5092]))]
     (.renameTo ^{:tag java.io.File} p0_5091 ^{:tag java.io.File} p1_5092)
     (tortilla.wrap/type-error "java.io.File::renameTo" p0_5091 p1_5092))))

(clojure.core/defn set-executable
  {:arglists '([java.io.File boolean] [java.io.File boolean boolean])}
  (^{:tag java.lang.Boolean}
   [p0_5094 p1_5095]
   (clojure.core/if-let [[p0_5094 p1_5095] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "setExecutable"
                                              java.io.File
                                              java.lang.Boolean/TYPE
                                              [java.lang.Boolean/TYPE]
                                              1
                                              #{}
                                              nil)
                                             [p0_5094 p1_5095]))]
     (.setExecutable ^{:tag java.io.File} p0_5094 ^{:tag java.lang.Boolean} p1_5095)
     (tortilla.wrap/type-error "java.io.File::setExecutable" p0_5094 p1_5095)))
  (^{:tag java.lang.Boolean}
   [p0_5097 p1_5098 p2_5099]
   (clojure.core/if-let [[p0_5097 p1_5098 p2_5099] (:invocation-args
                                                    (tortilla.wrap/args-compatible
                                                     (tortilla.wrap/->MemberInfo
                                                      -1
                                                      :method
                                                      "setExecutable"
                                                      java.io.File
                                                      java.lang.Boolean/TYPE
                                                      [java.lang.Boolean/TYPE
                                                       java.lang.Boolean/TYPE]
                                                      2
                                                      #{}
                                                      nil)
                                                     [p0_5097 p1_5098 p2_5099]))]
     (.setExecutable
^{:tag java.io.File}
p0_5097
^{:tag java.lang.Boolean}
p1_5098
^{:tag java.lang.Boolean}
p2_5099)
     (tortilla.wrap/type-error "java.io.File::setExecutable" p0_5097 p1_5098 p2_5099))))

(clojure.core/defn set-last-modified
  {:arglists '([java.io.File long])}
  (^{:tag java.lang.Boolean}
   [p0_5101 p1_5102]
   (clojure.core/if-let [[p0_5101 p1_5102] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "setLastModified"
                                              java.io.File
                                              java.lang.Boolean/TYPE
                                              [java.lang.Long/TYPE]
                                              1
                                              #{}
                                              nil)
                                             [p0_5101 p1_5102]))]
     (.setLastModified ^{:tag java.io.File} p0_5101 (clojure.core/long p1_5102))
     (tortilla.wrap/type-error "java.io.File::setLastModified" p0_5101 p1_5102))))

(clojure.core/defn set-read-only
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_5104]
   (clojure.core/if-let [[p0_5104] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "setReadOnly"
                                      java.io.File
                                      java.lang.Boolean/TYPE
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5104]))]
     (.setReadOnly ^{:tag java.io.File} p0_5104)
     (tortilla.wrap/type-error "java.io.File::setReadOnly" p0_5104))))

(clojure.core/defn set-readable
  {:arglists '([java.io.File boolean] [java.io.File boolean boolean])}
  (^{:tag java.lang.Boolean}
   [p0_5106 p1_5107]
   (clojure.core/if-let [[p0_5106 p1_5107] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "setReadable"
                                              java.io.File
                                              java.lang.Boolean/TYPE
                                              [java.lang.Boolean/TYPE]
                                              1
                                              #{}
                                              nil)
                                             [p0_5106 p1_5107]))]
     (.setReadable ^{:tag java.io.File} p0_5106 ^{:tag java.lang.Boolean} p1_5107)
     (tortilla.wrap/type-error "java.io.File::setReadable" p0_5106 p1_5107)))
  (^{:tag java.lang.Boolean}
   [p0_5109 p1_5110 p2_5111]
   (clojure.core/if-let [[p0_5109 p1_5110 p2_5111] (:invocation-args
                                                    (tortilla.wrap/args-compatible
                                                     (tortilla.wrap/->MemberInfo
                                                      -1
                                                      :method
                                                      "setReadable"
                                                      java.io.File
                                                      java.lang.Boolean/TYPE
                                                      [java.lang.Boolean/TYPE
                                                       java.lang.Boolean/TYPE]
                                                      2
                                                      #{}
                                                      nil)
                                                     [p0_5109 p1_5110 p2_5111]))]
     (.setReadable
^{:tag java.io.File}
p0_5109
^{:tag java.lang.Boolean}
p1_5110
^{:tag java.lang.Boolean}
p2_5111)
     (tortilla.wrap/type-error "java.io.File::setReadable" p0_5109 p1_5110 p2_5111))))

(clojure.core/defn set-writable
  {:arglists '([java.io.File boolean] [java.io.File boolean boolean])}
  (^{:tag java.lang.Boolean}
   [p0_5113 p1_5114]
   (clojure.core/if-let [[p0_5113 p1_5114] (:invocation-args
                                            (tortilla.wrap/args-compatible
                                             (tortilla.wrap/->MemberInfo
                                              -1
                                              :method
                                              "setWritable"
                                              java.io.File
                                              java.lang.Boolean/TYPE
                                              [java.lang.Boolean/TYPE]
                                              1
                                              #{}
                                              nil)
                                             [p0_5113 p1_5114]))]
     (.setWritable ^{:tag java.io.File} p0_5113 ^{:tag java.lang.Boolean} p1_5114)
     (tortilla.wrap/type-error "java.io.File::setWritable" p0_5113 p1_5114)))
  (^{:tag java.lang.Boolean}
   [p0_5116 p1_5117 p2_5118]
   (clojure.core/if-let [[p0_5116 p1_5117 p2_5118] (:invocation-args
                                                    (tortilla.wrap/args-compatible
                                                     (tortilla.wrap/->MemberInfo
                                                      -1
                                                      :method
                                                      "setWritable"
                                                      java.io.File
                                                      java.lang.Boolean/TYPE
                                                      [java.lang.Boolean/TYPE
                                                       java.lang.Boolean/TYPE]
                                                      2
                                                      #{}
                                                      nil)
                                                     [p0_5116 p1_5117 p2_5118]))]
     (.setWritable
^{:tag java.io.File}
p0_5116
^{:tag java.lang.Boolean}
p1_5117
^{:tag java.lang.Boolean}
p2_5118)
     (tortilla.wrap/type-error "java.io.File::setWritable" p0_5116 p1_5117 p2_5118))))

(clojure.core/defn to-path
  {:arglists '([java.io.File])}
  (^{:tag java.nio.file.Path}
   [p0_5120]
   (clojure.core/if-let [[p0_5120] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "toPath"
                                      java.io.File
                                      java.nio.file.Path
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5120]))]
     (.toPath ^{:tag java.io.File} p0_5120)
     (tortilla.wrap/type-error "java.io.File::toPath" p0_5120))))

(clojure.core/defn to-string
  {:arglists '([java.io.File])}
  (^{:tag java.lang.String}
   [p0_5122]
   (clojure.core/if-let [[p0_5122] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "toString"
                                      java.io.File
                                      java.lang.String
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5122]))]
     (.toString ^{:tag java.io.File} p0_5122)
     (tortilla.wrap/type-error "java.io.File::toString" p0_5122))))

(clojure.core/defn to-uri
  {:arglists '([java.io.File])}
  (^{:tag java.net.URI}
   [p0_5124]
   (clojure.core/if-let [[p0_5124] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "toURI"
                                      java.io.File
                                      java.net.URI
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5124]))]
     (.toURI ^{:tag java.io.File} p0_5124)
     (tortilla.wrap/type-error "java.io.File::toURI" p0_5124))))

(clojure.core/defn to-url
  {:arglists '([java.io.File])}
  (^{:tag java.net.URL}
   [p0_5126]
   (clojure.core/if-let [[p0_5126] (:invocation-args
                                    (tortilla.wrap/args-compatible
                                     (tortilla.wrap/->MemberInfo
                                      -1
                                      :method
                                      "toURL"
                                      java.io.File
                                      java.net.URL
                                      []
                                      0
                                      #{}
                                      nil)
                                     [p0_5126]))]
     (.toURL ^{:tag java.io.File} p0_5126)
     (tortilla.wrap/type-error "java.io.File::toURL" p0_5126))))
