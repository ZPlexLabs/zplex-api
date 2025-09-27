grammar Filters;

options {
    language=Java;
}

// supported filter queries
PLAYSTATE:        'playstate';
GENRES:           'genres';
PARENTAL_RATINGS:  'parentalratings';
STUDIOS:           'studios';
YEARS:             'years';
STATUS:           'status';

// possible values
ANY:              'any';
PLAYED:           'played';
UNPLAYED:         'unplayed';
CONTINUING:       'continuing';
ENDED:            'ended';

// rules to match list of identifiers (unquoted strings) and integers (allow empty lists)
listOfIntegers: (INT (COMMA INT)*)?;
listOfParentalRatings: (RATING (COMMA RATING)*)?;

// formatting lexers
COMMA:            ',';
LPAREN:           '(';
RPAREN:           ')';
INT:              [0-9]+;
RATING:          [a-z0-9+-]+;
WS:               [ \t\r\n]+ -> skip;

// Main filters rule
filters: filter (COMMA filter)*;

filter:
    playstate
  | genres
  | parentalRatings
  | studios
  | years
  | status;

playstate: PLAYSTATE LPAREN (ANY | PLAYED | UNPLAYED)? RPAREN;
genres: GENRES LPAREN listOfIntegers? RPAREN;
parentalRatings: PARENTAL_RATINGS LPAREN listOfParentalRatings? RPAREN;
studios: STUDIOS LPAREN listOfIntegers? RPAREN;
years: YEARS LPAREN listOfIntegers? RPAREN;
status: STATUS LPAREN (ANY | CONTINUING | ENDED)? RPAREN;