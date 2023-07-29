import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const Home(),
    );
  }
}

class Home extends StatefulWidget {
  const Home({super.key});

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  static const platform = MethodChannel('com.example.timer_app/timer');
  String result = "Timer is not Started yet";

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(),
      body: Center(
        child: Column(
          children: [
            ElevatedButton(
              onPressed: _startTimer,
              child: const Text("Start Timer"),
            ),
            Text(result),
          ],
        ),
      ),
    );
  }

  Future<void> _startTimer() async {
    String? state;
    try {
      state = await platform.invokeMethod('startTimer');

      print(state);
    } on PlatformException catch (e) {
      print(e.message);
    }
    setState(() {
      result = state ?? "returned is null";
    });
  }
}
