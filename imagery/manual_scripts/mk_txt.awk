BEGINFILE {
        phase = 0;
}

/<!-- NEWIMAGE2 -->/ {
        if (phase != 1) next;
        phase = 2;
        next;
}

{
        switch (phase) {
        case 0: break
        case 1: print $0
                break
        case 2: break
        }
}

/<!-- mode=normal -->/ {
        if (phase != 0) next;
        match($0, "<!-- mode=normal -->(.*)", a)
        printf "%s", a[1]
        phase = 1;
        next;
}
