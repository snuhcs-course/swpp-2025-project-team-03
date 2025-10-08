## Code For Dataset

### Dataset Structure

[Public Speech Dataset](https://www.aihub.or.kr/aihubdata/data/view.do?pageIndex=1&currMenu=115&topMenu=100&srchOneDataTy=DATA004&srchOptnCnd=OPTNCND001&searchKeyword=&srchDetailCnd=DETAILCND001&srchOrder=ORDER001&srchPagePer=20&srchDataRealmCode=REALM002&srchDataRealmCode=REALM010&aihubDataSe=data&dataSetSn=71663)

Remove Korean folder names and restructure as shown below

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
│ │ ├── college/
│ │ │ └── *_presentation.json
│ │ ├── high/
│ │ │ └── *_presentation.json
│ │ └── middle/
│ │ │ └── *_presentation.json
├── valid/
│ ├── data/
│ │ ├── college/
│ │ │ └── *.wav
│ │ ├── high/
│ │ │ └── *.wav
│ │ └── middle/
│ │ └── *.wav
│ └── label/
│ │ ├── college/
│ │ │ └── *_presentation.json
│ │ ├── high/
│ │ │ └── *_presentation.json
│ │ └── middle/
│ │ │ └── *_presentation.json
```

### Preprocess Dataset

```
# Command Examples
python mp4_to_wav.py --input_root "dataset/train"
python label_formatter.py --input_root "dataset/train/label"
python add_acoustic_features.py --input_root "dataset/train"
python add_semantic_features.py --input_root "dataset/train/label"

python mp4_to_wav.py --input_root "dataset/valid"
python label_formatter.py --input_root "dataset/valid/label"
python add_acoustic_features.py --input_root "dataset/valid"
python add_semantic_features.py --input_root "dataset/valid/label"
```

### Preprocess data with only wav & script

```
# Command Examples
python mp4_to_wav.py --input_root dataset/train
python make_only_script_data.py --root dataset/train
python add_acoustic_features.py --input_root dataset/train --label label_test
python add_features_from_script --input_root dataset/train --label label_test

python mp4_to_wav.py --input_root dataset/valid
python make_only_script_data.py --root dataset/valid
python add_acoustic_features.py --input_root dataset/valid --label label_test
python add_features_from_script --input_root dataset/valid --label label_test
```

- mp42wav.py: convert mp4 from every sub directory to wav
- label_formatter.py: trim labels from label/ (in-place)
- add_acoustics.py: add acoustic features (extracted from data/) to the existing label in label/
- add_semantic_features.py : add semantic features to the existing label in label/
- make_only_script_data.py : make json files containing only "eval_grade", "script" 
- add_features_from_script.py : add features using script to the existing label
