using System.Collections;
using System.Collections.Generic;
using System.Net.Sockets;
using UnityEngine;

public class LightControl : MonoBehaviour
{
    private TcpClient tcpClient;
    private NetworkStream stream;
    private byte[] buffer = new byte[1024];
    private string receivedData = "";

    public Light sceneLight;  // Unity에서 조작할 전등 (Light 컴포넌트)

    void Start()
    {
        // 서버의 IP 주소와 포트로 연결
        tcpClient = new TcpClient("172.17.4.163", 12346); // 서버 IP와 포트
        stream = tcpClient.GetStream();
    }

    void Update()
    {
        // 서버로부터 받은 데이터를 읽음
        if (stream.DataAvailable)
        {
            int bytesRead = stream.Read(buffer, 0, buffer.Length);
            receivedData = System.Text.Encoding.ASCII.GetString(buffer, 0, bytesRead);

            Debug.Log("받은 데이터: " + receivedData);

            // 받은 데이터로 전등 밝기 조절
            if (float.TryParse(receivedData, out float brightness))
            {
                AdjustLightBrightness(brightness);
            }
        }
    }

    void AdjustLightBrightness(float brightness)
    {
        // 밝기를 0~1 범위로 클램핑하여 Light 컴포넌트의 intensity에 적용
        sceneLight.intensity = Mathf.Clamp(brightness / 293.0f, 0f, 2.0f);
    }

    void OnApplicationQuit()
    {
        // 앱 종료 시 소켓 닫기
        stream.Close();
        tcpClient.Close();
    }
}

// public class light : MonoBehaviour
// {
//     public Light getLight;
//     public bool flashOn;


//     // Start is called before the first frame update
//     void Start()
//     {
//         if (flashOn)
//             getLight.intensity = 0;
//         else
//             getLight.intensity = 2;
//     }

//     // Update is called once per frame
//     void Update()
//     {
//         if (Input.GetKeyDown("q")){
//             flashOn = !flashOn;

//             // 플래시 상태에 따라 조명 밝기 변경
//             if (flashOn)
//                 getLight.intensity = 2;  // 켜짐 상태
//             else
//                 getLight.intensity = 0;
//         }
//     }
// }
