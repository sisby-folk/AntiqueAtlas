import os
import pandas

rename_dict = dict(pandas.read_csv('rename.csv').itertuples(False, None))

for path, directories, files in os.walk("./textures/gui/tiles"):
    for file in files:
        if file.endswith(".png"):
            os.rename(os.path.join(path, file), os.path.join(path, rename_dict[file.replace(".png", "")] + ".png"))

for path, directories, files in os.walk("./atlas/texture_sets"):
    for file in files:
        if file.endswith(".json"):
            with open(os.path.join(path, file), 'r') as f:
                content = f.read()
                for key, value in rename_dict.items():
                    content = content.replace('/{}"'.format(key), '/{}"'.format(value.replace(".png", "")))
            with open(os.path.join(path, file), 'w') as f:
                f.write(content)
