import requests

s3_key = input("Enter S3 key (e.g., pdf/0/5/xxxxx.pdf): ").strip()
upload_url = input("Enter presigned S3 upload URL: ").strip()
file_path = input("Enter your local pdf file path: ").strip()

upload_url = upload_url.replace("&amp;", "&")

print(f"\nUploading {s3_key} to S3...\n")

headers = {"Content-Type": "application/pdf"}

try:
    with open(file_path, "rb") as f:
        response = requests.put(upload_url, data=f, headers=headers)

    if response.status_code == 200:
        print("Upload successful!")
    else:
        print(f"Upload failed with status {response.status_code}")
        print(response.text)

except FileNotFoundError:
    print(f"File not found: {file_path}")
except Exception as e:
    print(f"Error occurred: {e}")
