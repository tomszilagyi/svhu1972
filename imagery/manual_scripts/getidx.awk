BEGIN {
	in_idx=0
	found=0
}

/<b>On this page/ {
	in_idx=1
	first=1
}

/<p><img/ {
	if (in_idx) {
		in_idx=0
		text=substr(text, 4)
		if (text ~ / \.\.\.$/) { # regular page: remove leading " - "
			text=substr(text, 1, length(text)-4)
		} else { # special (front/back) page: remove HTML tags
			while (match(text, "<[^<>]+>")) {
				text = substr(text, 1, RSTART-1) substr(text, RSTART+RLENGTH)
			}
		}
		found=1
		print text
	}
}

{
	if (in_idx==1) {
		if (first==1) {
			first=0
			text=""
		} else {
			text=text $0
		}
	}
}

END {
	if (!found) {
		print ""
	}
}
