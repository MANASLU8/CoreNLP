"""Parses Wiktionary to find masculine/feminine equivalents of the same nouns.

To run, first download a Wiktionary dump:

https://dumps.wikimedia.org/enwiktionary/latest/enwiktionary-latest-pages-articles.xml

and place it in the same directory as this script. The script outputs two files:
masc_to_fem.txt and fem_to_masc.txt, which can be copied and pasted into the MASC_TO_FEM
and FEM_TO_MASC constants, respectively, in Nouns.java.
"""

import re
import xml.etree.ElementTree as ET
from collections import defaultdict

RUSSIAN_SECTION_RE = re.compile(r"(?<!=)==Russian==(?!=)")
SECTION_RE = re.compile(r"(?<!=)==[^=]+==(?!=)")
NOUN_RE = re.compile(r"===Noun===\n{{(.*)}}")
GENDER_RE = re.compile(r"(m|f)(\d)*=(.+)")

masc_to_fem = defaultdict(list)
fem_to_masc = defaultdict(list)
with open("enwiktionary-latest-pages-articles.xml", "r", encoding="utf-8") as f_in:
    current_page = ""
    for line in f_in:
        if "<page>" in line or current_page:
            current_page += line
        if "</page>" in line and current_page:
            root = ET.fromstring(current_page)
            title = root.find("./title").text.lower()
            text_element = root.find("./revision/text")
            page_text = text_element.text
            if page_text:
                russian_section_match = RUSSIAN_SECTION_RE.search(page_text)
                if russian_section_match:
                    russian_section = page_text[russian_section_match.end():]
                    next_section_match = SECTION_RE.search(russian_section)
                    if next_section_match:
                        russian_section = russian_section[:next_section_match.start()]
                    noun_section = NOUN_RE.search(russian_section)
                    if noun_section:
                        fem_items = {}
                        masc_items = {}
                        for feature in noun_section.group(1).split("|"):
                            match = GENDER_RE.match(feature)
                            if match:
                                otherForm = match.group(3).replace("́", "").lower()
                                number = match.group(2)
                                if number is None:
                                    number = 1
                                else:
                                    number = int(number)
                                if match.group(1) == "m":
                                    masc_items[number] = otherForm
                                else:
                                    fem_items[number] = otherForm
                        if fem_items:
                            value = [x[1] for x in sorted(fem_items.items())]
                            if title in masc_to_fem and masc_to_fem[title] != value:
                                raise Exception(f"conflicting entries found for masc_to_fem['{title}']: {masc_to_fem[title]}/{value}")
                            masc_to_fem[title] = value
                        if masc_items:
                            value = [x[1] for x in sorted(masc_items.items())]
                            if title in fem_to_masc and fem_to_masc[title] != value:
                                raise Exception(f"conflicting entries found for fem_to_masc['{title}']: {fem_to_masc[title]}/{value}")
                            fem_to_masc[title] = value
            current_page = ""

for masc_word, fem_words in masc_to_fem.items():
    for fem_word in fem_words:
        if fem_word not in fem_to_masc:
            fem_to_masc[fem_word] = [masc_word]
        elif masc_word not in fem_to_masc[fem_word]:
            print(f"conflicting entries: masc_to_fem['{masc_word}'] = {fem_words}, fem_to_masc['{fem_word}'] = {fem_to_masc[fem_word]}")

for fem_word, masc_words in fem_to_masc.items():
    for masc_word in masc_words:
        if masc_word not in masc_to_fem:
            masc_to_fem[masc_word] = [fem_word]
        elif fem_word not in masc_to_fem[masc_word]:
            print(f"conflicting entries: masc_to_fem['{masc_word}'] = {masc_to_fem[masc_word]}, fem_to_masc['{fem_word}'] = {masc_words}")

with open("masc_to_fem.txt", "w", encoding="utf-8") as f_out:
    for masc_word, fem_words in masc_to_fem.items():
        f_out.write('{"%s", "%s"},\n' % (masc_word, fem_words[0]))

with open("fem_to_masc.txt", "w", encoding="utf-8") as f_out:
    for fem_word, masc_words in fem_to_masc.items():
        f_out.write('{"%s", "%s"},\n' % (fem_word, masc_words[0]))
