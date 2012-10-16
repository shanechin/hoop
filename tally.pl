#!/usr/bin/perl

$numIters = 10;
$numTeams = 6;

@names = ("G1-1", "G2: JaMeR v1", "g3", "G4", "Group 5", "G6-V4");
@displayNames = ("G1-1            ", "G2: JaMeR v1    ", "g3              ", "G4              ", "Group 5         ", "G6-V4           ");

%totalScore = ();
%totalRank = ();

for ($i = 0; $i < $numIters; $i++) {
	`java hoop/sim/Hoop &> out`;
	$result = `tail -$numTeams out`;
	#`rm out`;
	
	@lines = split(/\n/, $result);
	my $place = 0;
	my $lastScore = 1000000;
	my $tied = 1;
	for $line (@lines) {
		$line =~ s/([0-9]+)//;
		$score = $1;
		$line =~ /(\w.*\w)/;
		$team = $1;
		if ($score < $lastScore) {
			$place += $tied;
			$tied = 1;
			$lastScore = $score;
		}
		else {
			$tied++;
		}
		if (!defined $totalScore{$team}) {
			$totalScore{$team} = $score;
			$totalRank{$team} = $place;
		}
		else {
			$totalScore{$team} += $score;
			$totalRank{$team} += $place;
		}
	}
}

for ($i = 0; $i < $numTeams; $i++) {
	print "$displayNames[$i]", $totalScore{$names[$i]}/$numIters, "\t", $totalRank{$names[$i]}/$numIters, "\n";
}




