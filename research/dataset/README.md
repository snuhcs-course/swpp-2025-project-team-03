## Code For Dataset

### Dataset Structure

[Public Speech Dataset](https://www.aihub.or.kr/aihubdata/data/view.do?pageIndex=1&currMenu=115&topMenu=100&srchOneDataTy=DATA004&srchOptnCnd=OPTNCND001&searchKeyword=&srchDetailCnd=DETAILCND001&srchOrder=ORDER001&srchPagePer=20&srchDataRealmCode=REALM002&srchDataRealmCode=REALM010&aihubDataSe=data&dataSetSn=71663)

```
dataset/
├── train/
│ ├── data/
│ │ ├── college/
│ │ │ └── *.wav
│ │ ├── high/
│ │ │ └── *.wav
│ │ └── middle/
│ │ └── *.wav
│ └── label/
│ ├── college/
│ │ └── *_presentation.json
│ ├── high/
│ │ └── *_presentation.json
│ └── middle/
│ └── *_presentation.json
├── valid/
│ ├── data/
│ │ ├── college/
│ │ │ └── *.wav
│ │ ├── high/
│ │ │ └── *.wav
│ │ └── middle/
│ │ └── *.wav
│ └── label/
│ ├── college/
│ │ └── *_presentation.json
│ ├── high/
│ │ └── *_presentation.json
│ └── middle/
│ └── *_presentation.json
```

### Preprocess Dataset

- mp42wav.py: convert mp4 from every sub directory to wav
- label_formatter.py: trim labels from label_origin/ and save them to label_trim/
- add_acoustics.py: add acoustic features to the existing label in label_trim/ and save them to label/
