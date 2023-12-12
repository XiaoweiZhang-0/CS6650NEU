module example/main

go 1.21.3

replace example.com/albumStore => ./go

require example.com/albumStore v0.0.0-00010101000000-000000000000

require github.com/gorilla/mux v1.8.1 // indirect
