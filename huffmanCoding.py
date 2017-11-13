from heapq import heappush, heappop, heapify
from collections import defaultdict
import ast


def encode(symb2freq):
    """Huffman encode the given dict mapping symbols to weights"""
    heap = [[wt, [sym, ""]] for sym, wt in symb2freq.items()]
    heapify(heap)
    while len(heap) > 1:
        lo = heappop(heap)
        hi = heappop(heap)
        for pair in lo[1:]:
            pair[1] = '0' + pair[1]
        for pair in hi[1:]:
            pair[1] = '1' + pair[1]
        heappush(heap, [lo[0] + hi[0]] + lo[1:] + hi[1:])
    return sorted(heappop(heap)[1:], key=lambda p: (len(p[-1]), p))

def huffman_encode(txt):
    code_book = {}
    main_text = ''
    symb2freq = defaultdict(int)
    for ch in txt:
        symb2freq[ch] += 1

    huff = encode(symb2freq)
    for letter in txt:
        for p in huff:
            # code book creation
            code_book[p[0]] = p[1]
            # Character compare with Huffman Symbol
            if p[0] == letter:
                # Huffman coding and padding
                main_text = str(main_text) + str(p[1] + ' ')
                break

    coding = {'text': main_text, 'code': code_book}

    return str(coding)


def huffman_decode(compressed_text, code_book):

    text_codes = compressed_text.split(' ')

    clear_text = ''
    for code in text_codes:
        for k in code_book:
            if code_book[k] == code:
                clear_text = str(clear_text) + str(k)


    return clear_text

def test():
    fax = 'this is an example for huffman encoding'
    b = huffman_encode(fax)
    print b

    c = data = ast.literal_eval(b)
    print huffman_decode(c['text'], c['code'])