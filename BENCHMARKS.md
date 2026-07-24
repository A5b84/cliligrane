# Benchmarks

## Setup

The benchmarks were run on an 8-core laptop with one of the following datasets:

- Dataset A: a single 12-page PDF file
- Dataset B: 56 PDF files with a total of 180 pages

The only metric used was the duration to watermark all documents.

## Results

| Parallelization |  Commit   | Dataset A | Dataset B |
|:----------------|:---------:|----------:|----------:|
| None            | `e0a26ee` |  24.901 s | 312.890 s |
| Per document    | `3cf8229` |  24.790 s |  87.735 s |
| Per page        | `9675da3` |  12.265 s |  87.237 s |

Notes:

- Per-document parallelization can end up having only few threads active when longer documents start being processed late.
- Per-page parallelization can use more memory as there is no limit on the number of documents processed concurrently.
